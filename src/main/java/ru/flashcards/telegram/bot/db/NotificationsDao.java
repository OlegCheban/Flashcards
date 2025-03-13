package ru.flashcards.telegram.bot.db;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardSpacedRepetitionNotification;

import java.sql.Date;
import java.util.List;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class NotificationsDao {
    private final DSLContext dsl;

    public List<UserFlashcardPushMono> getUserFlashcardsRandomNotification(){
        var fpm = FLASHCARDS_PUSH_MONO.as("fpm");

        return dsl
                .select(fpm.USER_FLASHCARD_ID, fpm.WORD, fpm.DESCRIPTION, fpm.USER_ID, fpm.TRANSCRIPTION)
                .from(fpm)
                .where(fpm.SEND)
                .fetchInto(UserFlashcardPushMono.class);
    }

    public List<UserFlashcardSpacedRepetitionNotification> getUserFlashcardsSpacedRepetitionNotification(){
        /*
        SELECT irq.user_flashcard_id, irq.word, irq.description, irq.user_id, irq.notification_date, irq.transcription, irq.prc
        FROM main.interval_repetition_queue irq,
             main.user_flashcard uf
        where irq.user_flashcard_id = uf.id
          and uf.learned_date is not null
          and not exists (select 1
                          from main.flashcard_push_history fph
                          where irq.user_flashcard_id = fph.flashcard_id
                            and cast(irq.notification_date as date) = cast(fph.push_date as date))
        order by irq.notification_date
        */

        var irq = INTERVAL_REPETITION_QUEUE.as("irq");
        var uf = USER_FLASHCARD.as("uf");
        var fph = FLASHCARD_PUSH_HISTORY.as("fph");

        return dsl.select(irq.USER_FLASHCARD_ID, irq.WORD, irq.DESCRIPTION, irq.USER_ID, irq.NOTIFICATION_DATE, irq.TRANSCRIPTION, irq.PRC)
                .from(irq)
                .join(uf).on(irq.USER_FLASHCARD_ID.eq(uf.ID))
                .where(uf.LEARNED_DATE.isNotNull())
                .and(notExists(selectOne().from(fph)
                        .where(fph.FLASHCARD_ID.eq(irq.USER_FLASHCARD_ID)
                                .and(irq.NOTIFICATION_DATE.cast(Date.class).eq(fph.PUSH_DATE.cast(Date.class))))))
                .orderBy(irq.NOTIFICATION_DATE)
                .fetchInto(UserFlashcardSpacedRepetitionNotification.class);
    }

    public void updatePushTimestamp(Long flashcardId){
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.PUSH_TIMESTAMP, currentLocalDateTime())
                .where(USER_FLASHCARD.ID.eq(flashcardId))
                .execute();
    }

    public void addFlashcardPushHistory(Long flashcardId) {
        dsl.insertInto(FLASHCARD_PUSH_HISTORY)
                .values(COMMON_SEQ.nextval(), flashcardId, defaultValue(FLASHCARD_PUSH_HISTORY.PUSH_DATE))
                .execute();
    }

    public void refreshIntervalNotification(){
        String sql = "REFRESH MATERIALIZED VIEW main.interval_repetition_queue";
        dsl.execute(sql);
    }

    public Boolean isPushQueueUpToCurrentDate() {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(INTERVAL_REPETITION_QUEUE)
                        .where(INTERVAL_REPETITION_QUEUE.LAST_REFRESH.eq(currentLocalDate())));
    }

    public void setNotificationInterval(Integer minQty, Long chatId) {
        dsl.update(USER)
            .set(USER.NOTIFICATION_INTERVAL, minQty)
            .where(USER.CHAT_ID.eq(chatId))
            .execute();
    }

    public int deleteSpacedRepetitionHistory(Long flashcardId) {
        return dsl.delete(FLASHCARD_PUSH_HISTORY)
                .where(FLASHCARD_PUSH_HISTORY.FLASHCARD_ID.eq(flashcardId))
                .execute();
    }
}