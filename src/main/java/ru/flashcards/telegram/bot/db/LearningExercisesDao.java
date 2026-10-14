package ru.flashcards.telegram.bot.db;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.DONE_LEARN_EXERCISE_STAT;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARD;
import static org.jooq.codegen.maven.flashcards.Tables.LEARNING_EXERCISE_KIND;
import static org.jooq.codegen.maven.flashcards.Tables.NEXT_EXERCISE_QUEUE;
import static org.jooq.codegen.maven.flashcards.Tables.USER;
import static org.jooq.codegen.maven.flashcards.Tables.USER_EXERCISE_SETTINGS;
import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.noCondition;
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

import ru.flashcards.telegram.bot.botapi.ExerciseKinds;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.db.dto.ExerciseKind;
import ru.flashcards.telegram.bot.db.dto.SendToLearnFlashcard;

@Component
@RequiredArgsConstructor
public class LearningExercisesDao {

    private final DSLContext dsl;

    /**
     * @return the next exercise of the current learning session, empty when the session is over
     */
    public Optional<ExerciseFlashcard> findCurrentExerciseCard(Long chatId) {
        var queue = NEXT_EXERCISE_QUEUE.as("queue");
        return dsl.select(
                        queue.CHAT_ID,
                        queue.WORD,
                        queue.CODE,
                        queue.DESCRIPTION,
                        queue.TRANSCRIPTION,
                        queue.USER_FLASHCARD_ID,
                        queue.TRANSLATION,
                        queue.EXAMPLE
                )
                .from(queue)
                .where(queue.CHAT_ID.eq(chatId))
                .fetchOptionalInto(ExerciseFlashcard.class);
    }

    public void insertExerciseResult(Long userFlashcardId, ExerciseKinds exerciseKind, boolean isCorrectAnswer) {
        dsl.insertInto(DONE_LEARN_EXERCISE_STAT,
                        DONE_LEARN_EXERCISE_STAT.ID,
                        DONE_LEARN_EXERCISE_STAT.USER_FLASHCARD_ID,
                        DONE_LEARN_EXERCISE_STAT.EXERCISE_KIND_ID,
                        DONE_LEARN_EXERCISE_STAT.IS_CORRECT_ANSWER)
                .select(
                        select(
                                COMMON_SEQ.nextval(),
                                val(userFlashcardId),
                                LEARNING_EXERCISE_KIND.ID,
                                val(isCorrectAnswer))
                                .from(LEARNING_EXERCISE_KIND)
                                .where(LEARNING_EXERCISE_KIND.CODE.eq(exerciseKind.name())))
                .execute();
    }

    public void disableExercise(Long chatId, String exerciseCode) {
        dsl.deleteFrom(USER_EXERCISE_SETTINGS)
                .where(USER_EXERCISE_SETTINGS.USER_ID.eq(
                        select(USER.ID)
                                .from(USER)
                                .where(USER.CHAT_ID.eq(chatId))))
                .and(USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID.eq(
                        select(LEARNING_EXERCISE_KIND.ID)
                                .from(LEARNING_EXERCISE_KIND)
                                .where(LEARNING_EXERCISE_KIND.CODE.eq(exerciseCode))))
                .execute();
    }

    /**
     * Idempotent: a repeated button press does not create a duplicate setting.
     */
    public void enableExercise(Long chatId, String exerciseCode) {
        var ues = USER_EXERCISE_SETTINGS.as("ues");

        dsl.insertInto(USER_EXERCISE_SETTINGS,
                        USER_EXERCISE_SETTINGS.ID,
                        USER_EXERCISE_SETTINGS.USER_ID,
                        USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID)
                .select(
                        select(COMMON_SEQ.nextval(), USER.ID, LEARNING_EXERCISE_KIND.ID)
                                .from(USER)
                                .crossJoin(LEARNING_EXERCISE_KIND)
                                .where(USER.CHAT_ID.eq(chatId))
                                .and(LEARNING_EXERCISE_KIND.CODE.eq(exerciseCode))
                                .andNotExists(
                                        selectOne()
                                                .from(ues)
                                                .where(ues.USER_ID.eq(USER.ID))
                                                .and(ues.EXERCISE_KIND_ID.eq(LEARNING_EXERCISE_KIND.ID))))
                .execute();
    }

    public List<String> getRecentLearned(Long chatId, Long quantity, boolean withTranscription) {
        Field<String> flashcardLabel = withTranscription
                ? USER_FLASHCARD.WORD.concat(" \\[ ").concat(USER_FLASHCARD.TRANSCRIPTION).concat(" ]")
                : USER_FLASHCARD.WORD;

        return dsl.select(flashcardLabel)
                .from(USER_FLASHCARD)
                .join(USER).on(USER_FLASHCARD.USER_ID.eq(USER.ID))
                .where(USER.CHAT_ID.eq(chatId))
                .and(USER_FLASHCARD.LEARNED_DATE.isNotNull())
                .orderBy(USER_FLASHCARD.LEARNED_DATE.desc())
                .limit(quantity)
                .fetch(flashcardLabel);
    }

    public boolean existsExercise(Long chatId) {
        return dsl.fetchExists(
                selectOne()
                        .from(USER_FLASHCARD)
                        .join(USER).on(USER_FLASHCARD.USER_ID.eq(USER.ID))
                        .where(USER.CHAT_ID.eq(chatId))
                        .and(USER_FLASHCARD.LEARNED_DATE.isNull()));
    }

    public boolean existsLearnedFlashcards(Long chatId) {
        return dsl.fetchExists(
                selectOne()
                        .from(USER_FLASHCARD)
                        .join(USER).on(USER_FLASHCARD.USER_ID.eq(USER.ID))
                        .where(USER.CHAT_ID.eq(chatId))
                        .and(USER_FLASHCARD.LEARNED_DATE.isNotNull()));
    }

    public List<ExerciseKind> getExerciseKindToEnable(Long chatId) {
        return dsl.select(LEARNING_EXERCISE_KIND.CODE, LEARNING_EXERCISE_KIND.NAME)
                .from(LEARNING_EXERCISE_KIND)
                .whereNotExists(
                        selectOne()
                                .from(USER_EXERCISE_SETTINGS)
                                .where(LEARNING_EXERCISE_KIND.ID.eq(USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID))
                                .and(USER_EXERCISE_SETTINGS.USER_ID.eq(
                                        select(USER.ID)
                                                .from(USER)
                                                .where(USER.CHAT_ID.eq(chatId)))))
                .orderBy(LEARNING_EXERCISE_KIND.ORDER)
                .fetch(Records.mapping(ExerciseKind::new));
    }

    public List<ExerciseKind> getExerciseKindToDisable(Long chatId) {
        return dsl.select(LEARNING_EXERCISE_KIND.CODE, LEARNING_EXERCISE_KIND.NAME)
                .from(LEARNING_EXERCISE_KIND)
                .join(USER_EXERCISE_SETTINGS).on(LEARNING_EXERCISE_KIND.ID.eq(USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID))
                .where(USER_EXERCISE_SETTINGS.USER_ID.eq(
                        select(USER.ID)
                                .from(USER)
                                .where(USER.CHAT_ID.eq(chatId))))
                .orderBy(LEARNING_EXERCISE_KIND.ORDER)
                .fetch(Records.mapping(ExerciseKind::new));
    }

    public void deleteExerciseStat(Long userFlashcardId) {
        dsl.deleteFrom(DONE_LEARN_EXERCISE_STAT)
                .where(DONE_LEARN_EXERCISE_STAT.USER_FLASHCARD_ID.eq(userFlashcardId))
                .execute();
    }

    /**
     * @param flashcardWord exact word, {@code null} means any word
     */
    public List<SendToLearnFlashcard> getFlashcardsByWordToSuggestLearning(Long chatId, String flashcardWord) {
        var u = USER.as("u");
        var f = FLASHCARD.as("f");

        Condition wordCondition = flashcardWord == null ? noCondition() : f.WORD.eq(flashcardWord);
        var fc = lateral(
                select(f.ID, f.WORD, f.DESCRIPTION, f.TRANSLATION, f.TRANSCRIPTION)
                        .from(f)
                        .where(wordCondition)
                        .limit(1)
        ).as("fc");

        return dsl.select(
                        u.CHAT_ID,
                        fc.field(f.ID),
                        fc.field(f.DESCRIPTION),
                        fc.field(f.TRANSCRIPTION),
                        fc.field(f.TRANSLATION),
                        fc.field(f.WORD))
                .from(u)
                .crossJoin(fc)
                .where(u.CHAT_ID.eq(chatId))
                .fetch(Records.mapping(SendToLearnFlashcard::new));
    }

    public void setTrainingFlashcardsQuantity(int quantity, Long chatId) {
        dsl.update(USER)
                .set(USER.CARDS_PER_TRAINING, quantity)
                .where(USER.CHAT_ID.eq(chatId))
                .execute();
    }

    public void returnToLearn(Long userFlashcardId) {
        dsl.update(USER_FLASHCARD)
                .setNull(USER_FLASHCARD.LEARNED_DATE)
                .where(USER_FLASHCARD.ID.eq(userFlashcardId))
                .execute();
    }
}
