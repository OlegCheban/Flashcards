package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.ExerciseKinds;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;

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
}
