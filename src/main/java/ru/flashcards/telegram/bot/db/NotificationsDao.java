package ru.flashcards.telegram.bot.db;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARDS_PUSH_MONO;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARD_PUSH_HISTORY;
import static org.jooq.codegen.maven.flashcards.Tables.INTERVAL_REPETITION_QUEUE;
import static org.jooq.codegen.maven.flashcards.Tables.USER;
import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
import static org.jooq.impl.DSL.currentLocalDate;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.val;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardSpacedRepetitionNotification;

@Component
@RequiredArgsConstructor
public class NotificationsDao {

    private final DSLContext dsl;

    public List<UserFlashcardPushMono> getUserFlashcardsRandomNotification() {
        var fpm = FLASHCARDS_PUSH_MONO.as("fpm");

        return dsl
                .select(fpm.USER_FLASHCARD_ID, fpm.WORD, fpm.DESCRIPTION, fpm.USER_ID, fpm.TRANSCRIPTION)
                .from(fpm)
                .where(fpm.SEND)
                .fetchInto(UserFlashcardPushMono.class);
    }

    public List<UserFlashcardSpacedRepetitionNotification> getUserFlashcardsSpacedRepetitionNotification() {
        var irq = INTERVAL_REPETITION_QUEUE.as("irq");
        var uf = USER_FLASHCARD.as("uf");
        var fph = FLASHCARD_PUSH_HISTORY.as("fph");

        return dsl.select(irq.USER_FLASHCARD_ID, irq.WORD, irq.DESCRIPTION, irq.USER_ID, irq.NOTIFICATION_DATE,
                        irq.TRANSCRIPTION, irq.PRC)
                .from(irq)
                .join(uf).on(irq.USER_FLASHCARD_ID.eq(uf.ID))
                .where(uf.LEARNED_DATE.isNotNull())
                .andNotExists(selectOne()
                        .from(fph)
                        .where(fph.FLASHCARD_ID.eq(irq.USER_FLASHCARD_ID))
                        .and(irq.NOTIFICATION_DATE.cast(SQLDataType.LOCALDATE)
                                .eq(fph.PUSH_DATE.cast(SQLDataType.LOCALDATE))))
                .orderBy(irq.NOTIFICATION_DATE)
                .fetchInto(UserFlashcardSpacedRepetitionNotification.class);
    }

    public void updatePushTimestamp(Long userFlashcardId) {
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.PUSH_TIMESTAMP, currentLocalDateTime())
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .execute();
    }

    /**
     * push_date is filled by the column default.
     */
    public void addFlashcardPushHistory(Long userFlashcardId) {
        dsl.insertInto(FLASHCARD_PUSH_HISTORY, FLASHCARD_PUSH_HISTORY.ID, FLASHCARD_PUSH_HISTORY.FLASHCARD_ID)
                .values(COMMON_SEQ.nextval(), val(userFlashcardId))
                .execute();
    }

    public void refreshIntervalNotification() {
        // jOOQ DSL has no REFRESH MATERIALIZED VIEW; the statement is a constant without parameters
        dsl.execute("REFRESH MATERIALIZED VIEW main.interval_repetition_queue");
    }

    public boolean isPushQueueUpToCurrentDate() {
        return dsl.fetchExists(
                selectOne()
                        .from(INTERVAL_REPETITION_QUEUE)
                        .where(INTERVAL_REPETITION_QUEUE.LAST_REFRESH.eq(currentLocalDate())));
    }

    public void setNotificationInterval(int minutes, Long chatId) {
        dsl.update(USER)
                .set(USER.NOTIFICATION_INTERVAL, minutes)
                .where(USER.CHAT_ID.eq(chatId))
                .execute();
    }

    public void deleteSpacedRepetitionHistory(Long userFlashcardId) {
        dsl.deleteFrom(FLASHCARD_PUSH_HISTORY)
                .where(FLASHCARD_PUSH_HISTORY.FLASHCARD_ID.eq(userFlashcardId))
                .execute();
    }
}
