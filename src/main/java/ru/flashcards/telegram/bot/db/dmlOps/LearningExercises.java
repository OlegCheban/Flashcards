package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.ExerciseKinds;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;

@Component
@RequiredArgsConstructor
public class LearningExercises {
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

    public int insertExerciseResult(Long userFlashcardId, ExerciseKinds exerciseKinds, Boolean result) {
        var doneLearnExerciseStat = DONE_LEARN_EXERCISE_STAT;
        var learningExerciseKind = LEARNING_EXERCISE_KIND;

        return dsl.insertInto(doneLearnExerciseStat)
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
}
