package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.ExerciseKinds;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class UserProfileFlashcards {
    private final DSLContext dsl;
    public List<String> findUnlearnedFlashcardKeyword(Long chatId, Integer flashcardQuantity){
        /*
        select word
        from (select uf.word,
                     row_number() over (order by uf.nearest_training desc, uf.id) rn
              from main.user_flashcard uf,
                   main.user u
              where u.id = uf.user_id
                and u.chat_id = ?
                and uf.learned_date is null) x
        where x.rn <= ?
        */
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
    /*
     select x.word
     from (select uf.user_id, uf.id  user_flashcard_id, uf.word, u.cards_per_training,
             row_number() over (partition by uf.user_id
                 order by uf.nearest_training desc, uf.id) rn
      from main.user_flashcard uf
               join main.user u on uf.user_id = u.id
               join main.flashcard f on f.word = uf.word
      where u.chat_id = ?
        and uf.learned_date is null) x
     where x.rn <= x.cards_per_training
     */
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

    public void refreshLearnedFlashcards(){
        var userFlashcard = USER_FLASHCARD.as("a");
        var learnedStat = LEARNED_FLASHCARDS_STAT.as("b");

        dsl.update(userFlashcard)
                .set(userFlashcard.LEARNED_DATE, currentLocalDateTime())
                .from(learnedStat)
                .where(userFlashcard.ID.eq(learnedStat.USER_FLASHCARD_ID))
                .execute();
    }

    public int insertExerciseResult(Long userFlashcardId, ExerciseKinds exerciseKinds, Boolean result) {
        /*
            insert into main.done_learn_exercise_stat (id, user_flashcard_id, exercise_kind_id, is_correct_answer) values
            (nextval('main.common_seq'), 1, (select id from main.learning_exercise_kind where code = ?), ?)
        */
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
