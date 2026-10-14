package ru.flashcards.telegram.bot.db;

import static org.jooq.codegen.maven.flashcards.Tables.USER;
import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
import static org.jooq.impl.DSL.currentLocalDateTime;

import java.util.Objects;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

@Component
@RequiredArgsConstructor
public class WateringSessionsDao {
    /**
     * Same as the column default of user.watering_session_reply_time.
     */
    private static final int DEFAULT_REPLY_TIME_SECONDS = 5;

    private final DSLContext dsl;

    /**
     * @return the learned flashcard that has not been repeated for the longest time
     */
    public Optional<UserFlashcard> getUserFlashcardForWateringSession(Long chatId) {
        var uf = USER_FLASHCARD.as("uf");
        var u = USER.as("u");

        return dsl.select(uf.ID, uf.DESCRIPTION, uf.TRANSCRIPTION, uf.TRANSLATION, uf.WORD)
                .from(uf)
                .join(u).on(uf.USER_ID.eq(u.ID))
                .where(uf.LEARNED_DATE.isNotNull())
                .and(u.CHAT_ID.eq(chatId))
                .orderBy(uf.WATERING_SESSION_DATE.asc().nullsFirst(), uf.ID)
                .limit(1)
                .fetchOptional(Records.mapping(UserFlashcard::new));
    }

    public void finishedLastFlashcard(Long userFlashcardId) {
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.WATERING_SESSION_DATE, currentLocalDateTime())
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .execute();
    }

    public void setWateringSessionReplyTime(int seconds, Long chatId) {
        dsl.update(USER)
                .set(USER.WATERING_SESSION_REPLY_TIME, seconds)
                .where(USER.CHAT_ID.eq(chatId))
                .execute();
    }

    public int getWateringSessionReplyTime(Long chatId) {
        Integer replyTime = dsl.select(USER.WATERING_SESSION_REPLY_TIME)
                .from(USER)
                .where(USER.CHAT_ID.eq(chatId))
                .fetchOne(USER.WATERING_SESSION_REPLY_TIME);
        return Objects.requireNonNullElse(replyTime, DEFAULT_REPLY_TIME_SECONDS);
    }
}
