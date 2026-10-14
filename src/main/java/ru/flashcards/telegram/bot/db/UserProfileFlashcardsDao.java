package ru.flashcards.telegram.bot.db;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.EXCEPTED_USER_FLASHCARD;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARD;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARD_EXAMPLES;
import static org.jooq.codegen.maven.flashcards.Tables.LEARNED_FLASHCARDS_STAT;
import static org.jooq.codegen.maven.flashcards.Tables.USER;
import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
import static org.jooq.impl.DSL.currentLocalDateTime;
import static org.jooq.impl.DSL.orderBy;
import static org.jooq.impl.DSL.partitionBy;
import static org.jooq.impl.DSL.rowNumber;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.val;

import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Records;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

/**
 * Flashcards of a user's profile. Methods that address a user flashcard by id also take the chat id
 * and touch the row only if it belongs to that user: ids arrive in callback data, which a client can forge.
 */
@Component
@RequiredArgsConstructor
public class UserProfileFlashcardsDao {

    private final DSLContext dsl;

    public List<String> findUnlearnedFlashcardKeyword(Long chatId, int flashcardQuantity) {
        var uf = USER_FLASHCARD.as("uf");
        var user = USER.as("u");
        Field<Integer> rowNumber = rowNumber().over(orderBy(uf.NEAREST_TRAINING.desc(), uf.ID)).as("rn");

        var subquery = select(uf.WORD, rowNumber)
                .from(uf)
                .join(user).on(user.ID.eq(uf.USER_ID))
                .where(user.CHAT_ID.eq(chatId))
                .and(uf.LEARNED_DATE.isNull())
                .asTable("x");

        return dsl.select(subquery.field(uf.WORD))
                .from(subquery)
                .where(subquery.field(rowNumber).le(flashcardQuantity))
                .fetch(subquery.field(uf.WORD));
    }

    public List<String> findUserCardsForTraining(Long chatId) {
        var uf = USER_FLASHCARD.as("uf");
        var user = USER.as("u");
        var flashcard = FLASHCARD.as("f");
        Field<Integer> rowNumber = rowNumber()
                .over(partitionBy(uf.USER_ID).orderBy(uf.NEAREST_TRAINING.desc(), uf.ID))
                .as("rn");

        var subquery = select(uf.WORD, user.CARDS_PER_TRAINING, rowNumber)
                .from(uf)
                .join(user).on(uf.USER_ID.eq(user.ID))
                .join(flashcard).on(flashcard.WORD.eq(uf.WORD))
                .where(user.CHAT_ID.eq(chatId))
                .and(uf.LEARNED_DATE.isNull())
                .asTable("x");

        return dsl.select(subquery.field(uf.WORD))
                .from(subquery)
                .where(subquery.field(rowNumber).le(subquery.field(user.CARDS_PER_TRAINING)))
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

    public void refreshLearnedFlashcards() {
        var userFlashcard = USER_FLASHCARD.as("a");
        var learnedStat = LEARNED_FLASHCARDS_STAT.as("b");

        dsl.update(userFlashcard)
                .set(userFlashcard.LEARNED_DATE, currentLocalDateTime())
                .from(learnedStat)
                .where(userFlashcard.ID.eq(learnedStat.USER_FLASHCARD_ID))
                .execute();
    }

    /**
     * Adds the flashcard to the user's profile in a single statement
     * unless a flashcard with the same word is already there.
     *
     * @return {@code true} if the flashcard has been added
     */
    public boolean addUserFlashcardIfAbsent(Long chatId, Flashcard flashcard) {
        var existing = USER_FLASHCARD.as("existing");

        int inserted = dsl.insertInto(USER_FLASHCARD,
                        USER_FLASHCARD.ID,
                        USER_FLASHCARD.WORD,
                        USER_FLASHCARD.DESCRIPTION,
                        USER_FLASHCARD.TRANSCRIPTION,
                        USER_FLASHCARD.TRANSLATION,
                        USER_FLASHCARD.CATEGORY_ID,
                        USER_FLASHCARD.USER_ID,
                        USER_FLASHCARD.PUSH_TIMESTAMP)
                .select(
                        select(
                                COMMON_SEQ.nextval(),
                                val(flashcard.word()),
                                val(flashcard.description()),
                                val(flashcard.transcription()),
                                val(flashcard.translation()),
                                val(flashcard.categoryId()),
                                USER.ID,
                                currentLocalDateTime())
                                .from(USER)
                                .where(USER.CHAT_ID.eq(chatId))
                                .andNotExists(
                                        selectOne()
                                                .from(existing)
                                                .where(existing.USER_ID.eq(USER.ID))
                                                .and(existing.WORD.eq(flashcard.word()))))
                .execute();
        return inserted > 0;
    }

    public boolean boostUserFlashcardPriority(Long chatId, Long userFlashcardId) {
        return dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.NEAREST_TRAINING, 1)
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .and(ownedBy(chatId))
                .execute() > 0;
    }

    public boolean editTranslation(Long chatId, Long userFlashcardId, String translation) {
        return dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.TRANSLATION, translation)
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .and(ownedBy(chatId))
                .execute() > 0;
    }

    public Optional<UserFlashcard> findUserFlashcardById(Long chatId, Long userFlashcardId) {
        return dsl.select(
                    USER_FLASHCARD.ID,
                    USER_FLASHCARD.DESCRIPTION,
                    USER_FLASHCARD.TRANSCRIPTION,
                    USER_FLASHCARD.TRANSLATION,
                    USER_FLASHCARD.WORD)
                .from(USER_FLASHCARD)
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .and(ownedBy(chatId))
                .fetchOptional(Records.mapping(UserFlashcard::new));
    }

    public Optional<UserFlashcard> findUserFlashcardByName(Long chatId, String name) {
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
                .orderBy(USER_FLASHCARD.ID)
                .limit(1)
                .fetchOptional(Records.mapping(UserFlashcard::new));
    }

    public List<String> getExamplesByUserFlashcardId(Long userFlashcardId) {
        return dsl.select(FLASHCARD_EXAMPLES.EXAMPLE)
                .from(FLASHCARD_EXAMPLES)
                .join(FLASHCARD).on(FLASHCARD.ID.eq(FLASHCARD_EXAMPLES.FLASHCARD_ID))
                .join(USER_FLASHCARD).on(USER_FLASHCARD.WORD.eq(FLASHCARD.WORD))
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .fetch(FLASHCARD_EXAMPLES.EXAMPLE);
    }

    /**
     * Idempotent: a repeated button press does not create a duplicate exclusion.
     */
    public void exceptFlashcard(Long chatId, Long flashcardId) {
        var excepted = EXCEPTED_USER_FLASHCARD.as("excepted");

        dsl.insertInto(EXCEPTED_USER_FLASHCARD, EXCEPTED_USER_FLASHCARD.USER_ID, EXCEPTED_USER_FLASHCARD.FLASHCARD_ID)
                .select(
                        select(USER.ID, val(flashcardId))
                                .from(USER)
                                .where(USER.CHAT_ID.eq(chatId))
                                .andNotExists(
                                        selectOne()
                                                .from(excepted)
                                                .where(excepted.USER_ID.eq(USER.ID))
                                                .and(excepted.FLASHCARD_ID.eq(flashcardId))))
                .execute();
    }

    public void removeUserFlashcard(Long chatId, Long userFlashcardId) {
        dsl.deleteFrom(USER_FLASHCARD)
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .and(ownedBy(chatId))
                .execute();
    }

    private Condition ownedBy(Long chatId) {
        return USER_FLASHCARD.USER_ID.eq(
                select(USER.ID)
                        .from(USER)
                        .where(USER.CHAT_ID.eq(chatId)));
    }
}
