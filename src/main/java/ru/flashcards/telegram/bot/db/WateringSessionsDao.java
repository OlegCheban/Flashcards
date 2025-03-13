package ru.flashcards.telegram.bot.db;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.currentLocalDateTime;

@Component
@RequiredArgsConstructor
public class WateringSessionsDao {

    private final DSLContext dsl;

    public UserFlashcard getUserFlashcardForWateringSession(Long chatId) {
        var uf = USER_FLASHCARD.as("uf");
        var u = USER.as("u");

        return dsl.select(uf.ID, uf.DESCRIPTION, uf.TRANSCRIPTION, uf.TRANSLATION, uf.WORD)
                .from(uf)
                .join(u).on(uf.USER_ID.eq(u.ID)).where(uf.LEARNED_DATE.isNotNull()).and(u.CHAT_ID.eq(chatId))
                .orderBy(uf.WATERING_SESSION_DATE.nullsFirst(), uf.ID).limit(1)
                .fetchOneInto(UserFlashcard.class);
    }

    public void finishedLastFlashcard(Long userFlashcardId) {
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.WATERING_SESSION_DATE, currentLocalDateTime())
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .execute();
    }

    public void setWateringSessionReplyTime(Integer seconds, Long chatId) {
        dsl.update(USER)
                .set(USER.WATERING_SESSION_REPLY_TIME, seconds)
                .where(USER.CHAT_ID.eq(chatId))
                .execute();
    }

    public int getWateringSessionReplyTime(Long chatId){
        return dsl.select(USER.WATERING_SESSION_REPLY_TIME)
                .from(USER)
                .where(USER.CHAT_ID.eq(chatId))
                .execute();
    }

    public List<String> getRandomTranslations() {
        return dsl
                .select(RANDOM_FLASHCARD.TRANSLATION)
                .from(RANDOM_FLASHCARD)
                .fetch(RANDOM_FLASHCARD.TRANSLATION);
    }

    public List<String> getRandomWords() {
        return dsl
                .select(RANDOM_FLASHCARD.WORD)
                .from(RANDOM_FLASHCARD)
                .fetch(RANDOM_FLASHCARD.WORD);
    }
}
