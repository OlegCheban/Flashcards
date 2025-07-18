package ru.flashcards.telegram.bot.db;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class UserProfileFlashcardsDao {

    private final DSLContext dsl;

    public List<String> findUnlearnedFlashcardKeyword(Long chatId, Integer flashcardQuantity){
        var uf = USER_FLASHCARD.as("uf");
        var user =  USER.as("u");

        var subquery = select(uf.WORD, rowNumber().over(orderBy(uf.NEAREST_TRAINING.desc(), uf.ID)).as("rn"))
                .from(uf)
                .join(user).on(user.ID.eq(uf.USER_ID))
                .where(user.CHAT_ID.eq(chatId))
                .and(uf.LEARNED_DATE.isNull())
                .asTable("x");

        return dsl
                .select(subquery.field(uf.WORD))
                .from(subquery)
                .where(subquery.field("rn", Integer.class).le(flashcardQuantity))
                .fetch(subquery.field(uf.WORD));

    }

    public List<String> findUserCardsForTraining(Long chatId) {
        var uf = USER_FLASHCARD.as("uf");
        var user = USER.as("u");
        var flashcard = FLASHCARD.as("f");

        var subquery = dsl.select(
                        uf.USER_ID,
                        uf.ID.as("user_flashcard_id"),
                        uf.WORD,
                        user.CARDS_PER_TRAINING,
                        rowNumber()
                                .over(partitionBy(uf.USER_ID)
                                        .orderBy(uf.NEAREST_TRAINING.desc(), uf.ID))
                                .as("rn")
                )
                .from(uf)
                .join(user).on(uf.USER_ID.eq(user.ID))
                .join(flashcard).on(flashcard.WORD.eq(uf.WORD))
                .where(user.CHAT_ID.eq(chatId))
                .and(uf.LEARNED_DATE.isNull())
                .asTable("x");

        return dsl.select(subquery.field(uf.WORD))
                .from(subquery)
                .where(subquery.field("rn", Integer.class).le(subquery.field(user.CARDS_PER_TRAINING)))
                .fetch(subquery.field(uf.WORD));
    }

    public List<String> findLearnedFlashcards(Long chatId) {
        var userFlashcard = USER_FLASHCARD.as("a");
        var learnedFlashcardsStat = LEARNED_FLASHCARDS_STAT.as("b");
        var user = USER.as("u");

        return dsl.select(userFlashcard.WORD)
                .from(userFlashcard)
                .join(learnedFlashcardsStat).on(userFlashcard.ID.eq(learnedFlashcardsStat.USER_FLASHCARD_ID))
                .join(user).on(userFlashcard.USER_ID.eq(user.ID))
                .where(user.CHAT_ID.eq(chatId))
                .fetch(userFlashcard.WORD);
    }

    public void refreshLearnedFlashcards(){
        var userFlashcard = USER_FLASHCARD.as("a");
        var learnedStat = LEARNED_FLASHCARDS_STAT.as("b");

        dsl.update(userFlashcard)
                .set(userFlashcard.LEARNED_DATE, currentLocalDateTime())
                .from(learnedStat)
                .where(userFlashcard.ID.eq(learnedStat.USER_FLASHCARD_ID))
                .execute();
    }

    public void addUserFlashcard(String word, String description, String transcription, String translation, Long categoryId, Long chatId) {
        dsl.insertInto(USER_FLASHCARD)
            .set(USER_FLASHCARD.ID, COMMON_SEQ.nextval())
            .set(USER_FLASHCARD.WORD, word)
            .set(USER_FLASHCARD.DESCRIPTION, description)
            .set(USER_FLASHCARD.TRANSCRIPTION, transcription)
            .set(USER_FLASHCARD.TRANSLATION, translation)
            .set(USER_FLASHCARD.CATEGORY_ID, categoryId)
            .set(USER_FLASHCARD.USER_ID,
                dsl.select(USER.ID)
                   .from(USER)
                   .where(USER.CHAT_ID.eq(chatId)))
            .set(USER_FLASHCARD.PUSH_TIMESTAMP, currentLocalDateTime())
            .execute();
    }

    public void boostUserFlashcardPriority(Long userFlashcardId) {
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.NEAREST_TRAINING, 1)
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .execute();
    }

    public void editTranslation(Long flashcardId, String translation) {
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.TRANSLATION, translation)
                .where(USER_FLASHCARD.ID.eq(flashcardId))
                .execute();
    }

    public UserFlashcard findUserFlashcardById(Long flashcardId) {
        return dsl.select(
                    USER_FLASHCARD.ID,
                    USER_FLASHCARD.DESCRIPTION,
                    USER_FLASHCARD.TRANSCRIPTION,
                    USER_FLASHCARD.TRANSLATION,
                    USER_FLASHCARD.WORD)
                .from(USER_FLASHCARD)
                .where(USER_FLASHCARD.ID.eq(flashcardId))
                .fetchOneInto(UserFlashcard.class);
    }

    public UserFlashcard findUserFlashcardByName(Long chatId, String name) {
        return dsl.select(
                    USER_FLASHCARD.ID,
                    USER_FLASHCARD.DESCRIPTION,
                    USER_FLASHCARD.TRANSCRIPTION,
                    USER_FLASHCARD.TRANSLATION,
                    USER_FLASHCARD.WORD)
                .from(USER_FLASHCARD)
                .join(USER).on(USER.ID.eq(USER_FLASHCARD.USER_ID))
                .where(USER.CHAT_ID.eq(chatId))
                .and(USER_FLASHCARD.WORD.eq(name))
                .fetchOneInto(UserFlashcard.class);
    }

    public List<String> getExamplesByUserFlashcardId(Long userFlashcardId) {
        return dsl.select(FLASHCARD_EXAMPLES.EXAMPLE)
                .from(FLASHCARD_EXAMPLES)
                .join(FLASHCARD).on(FLASHCARD.ID.eq(FLASHCARD_EXAMPLES.FLASHCARD_ID))
                .join(USER_FLASHCARD).on(USER_FLASHCARD.WORD.eq(FLASHCARD.WORD))
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .fetchInto(String.class);
    }

    public void exceptFlashcard(Long chatId, Long flashcardId) {
            dsl.insertInto(EXCEPTED_USER_FLASHCARD)
                .set(EXCEPTED_USER_FLASHCARD.USER_ID, 
                    dsl.select(USER.ID)
                       .from(USER)
                       .where(USER.CHAT_ID.eq(chatId)))
                .set(EXCEPTED_USER_FLASHCARD.FLASHCARD_ID, flashcardId)
                .execute();
    }
}
