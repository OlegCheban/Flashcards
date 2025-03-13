package ru.flashcards.telegram.bot.db;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.ExerciseKinds;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.db.dto.ExerciseKind;
import ru.flashcards.telegram.bot.db.dto.SendToLearnFlashcard;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class LearningExercisesDao {
    private final DSLContext dsl;

    public ExerciseFlashcard findCurrentExerciseCard(Long chatId) {
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
                .fetchOneInto(ExerciseFlashcard.class);
    }

    public void insertExerciseResult(Long userFlashcardId, ExerciseKinds exerciseKinds, Boolean result) {
        var doneLearnExerciseStat = DONE_LEARN_EXERCISE_STAT;
        var learningExerciseKind = LEARNING_EXERCISE_KIND;

        dsl.insertInto(doneLearnExerciseStat)
                .values(
                        COMMON_SEQ.nextval(),
                        userFlashcardId,
                        dsl.select(learningExerciseKind.ID)
                                .from(learningExerciseKind)
                                .where(learningExerciseKind.CODE.eq(exerciseKinds.name()))
                                .fetchOne(learningExerciseKind.ID),
                        result
                )
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

    public List<String> getRandomDescriptions() {
        return dsl
                .select(RANDOM_FLASHCARD.DESCRIPTION)
                .from(RANDOM_FLASHCARD)
                .fetch(RANDOM_FLASHCARD.DESCRIPTION);
    }

    public void disableExercise(Long chatId, String exerciseCode) {
            dsl.delete(USER_EXERCISE_SETTINGS)
                .where(USER_EXERCISE_SETTINGS.USER_ID.eq(
                        dsl.select(USER.ID)
                           .from(USER)
                           .where(USER.CHAT_ID.eq(chatId))
                ))
                .and(USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID.eq(
                        dsl.select(LEARNING_EXERCISE_KIND.ID)
                           .from(LEARNING_EXERCISE_KIND)
                           .where(LEARNING_EXERCISE_KIND.CODE.eq(exerciseCode))
                ))
                .execute();
    }

    public void enableExercise(Long chatId, String exerciseCode) {
            dsl.insertInto(USER_EXERCISE_SETTINGS)
                .values(
                    COMMON_SEQ.nextval(),
                    dsl.select(USER.ID)
                       .from(USER)
                       .where(USER.CHAT_ID.eq(chatId)),

                    dsl.select(LEARNING_EXERCISE_KIND.ID)
                       .from(LEARNING_EXERCISE_KIND)
                       .where(LEARNING_EXERCISE_KIND.CODE.eq(exerciseCode))
                )
                .execute();
    }


    public List<String> getRecentLearned(Long chatId, Long quantity) {
        return dsl.select(USER_FLASHCARD.WORD.concat(" [ ").concat(USER_FLASHCARD.TRANSCRIPTION).concat(" ]"))
                .from(USER_FLASHCARD)
                .join(USER).on(USER_FLASHCARD.USER_ID.eq(USER.ID))
                .where(USER.CHAT_ID.eq(chatId))
                .and(USER_FLASHCARD.LEARNED_DATE.isNotNull())
                .orderBy(USER_FLASHCARD.LEARNED_DATE.desc())
                .limit(quantity)
                .fetchInto(String.class);
    }

    public Boolean existsExercise(Long chatId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_FLASHCARD)
                        .join(USER).on(USER_FLASHCARD.USER_ID.eq(USER.ID))
                        .where(USER.CHAT_ID.eq(chatId))
                        .and(USER_FLASHCARD.LEARNED_DATE.isNull())
                        .limit(1)
        );
    }

    public Boolean existsLearnedFlashcards(Long chatId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USER_FLASHCARD)
                        .join(USER).on(USER_FLASHCARD.USER_ID.eq(USER.ID))
                        .where(USER.CHAT_ID.eq(chatId))
                        .and(USER_FLASHCARD.LEARNED_DATE.isNotNull())
                        .limit(1)
        );
    }

    public List<ExerciseKind> getExerciseKindToEnable(Long chatId) {
        return dsl.select(LEARNING_EXERCISE_KIND.CODE, LEARNING_EXERCISE_KIND.NAME)
                .from(LEARNING_EXERCISE_KIND)
                .whereNotExists(
                        dsl.selectOne()
                                .from(USER_EXERCISE_SETTINGS)
                                .where(LEARNING_EXERCISE_KIND.ID.eq(USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID))
                                .and(USER_EXERCISE_SETTINGS.USER_ID.eq(
                                        dsl.select(USER.ID)
                                                .from(USER)
                                                .where(USER.CHAT_ID.eq(chatId))
                                ))
                )
                .orderBy(LEARNING_EXERCISE_KIND.ORDER)
                .fetchInto(ExerciseKind.class);
    }

    public List<ExerciseKind> getExerciseKindToDisable(Long chatId) {
        return dsl.select(LEARNING_EXERCISE_KIND.CODE, LEARNING_EXERCISE_KIND.NAME)
                .from(LEARNING_EXERCISE_KIND)
                .join(USER_EXERCISE_SETTINGS).on(LEARNING_EXERCISE_KIND.ID.eq(USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID))
                .where(USER_EXERCISE_SETTINGS.USER_ID.eq(
                        dsl.select(USER.ID)
                                .from(USER)
                                .where(USER.CHAT_ID.eq(chatId))
                ))
                .orderBy(LEARNING_EXERCISE_KIND.ORDER)
                .fetchInto(ExerciseKind.class);
    }

    public int deleteExerciseStat(Long flashcardId) {
        return dsl.delete(DONE_LEARN_EXERCISE_STAT)
                .where(DONE_LEARN_EXERCISE_STAT.USER_FLASHCARD_ID.eq(flashcardId))
                .execute();
    }

    public List<SendToLearnFlashcard> getFlashcardsByWordToSuggestLearning(Long chatId, String flashcardWord) {
        var u = USER.as("u");
        var f = FLASHCARD.as("f");
        
        return dsl.select(
                    u.CHAT_ID,
                        field("fc.flashcard_id", Long.class),
                        field("fc.word", String.class),
                        field("fc.description", String.class),
                        field("fc.translation", String.class),
                        field("fc.transcription", String.class)
                )
                .from(u)
                .join(lateral(
                        dsl.select(
                            f.ID.as("flashcard_id"),
                            f.WORD.as("word"),
                            f.DESCRIPTION.as("description"),
                            f.TRANSLATION.as("translation"),
                            f.TRANSCRIPTION.as("transcription")
                        ).from(f).where(f.WORD.eq(coalesce(val(flashcardWord), f.WORD))).limit(1))
                        .as("fc")
                ).on(trueCondition())
                .where(u.CHAT_ID.eq(chatId))
                .fetchInto(SendToLearnFlashcard.class);
    }

    public int setTrainingFlashcardsQuantity(Integer qty, Long chatId) {
        return dsl.update(USER)
                .set(USER.CARDS_PER_TRAINING, qty)
                .where(USER.CHAT_ID.eq(chatId))
                .execute();
    }

    public int returnToLearn(Long flashcardId) {
        return dsl.update(USER_FLASHCARD)
                .setNull(USER_FLASHCARD.LEARNED_DATE)
                .where(USER_FLASHCARD.ID.eq(flashcardId))
                .execute();
    }
}
