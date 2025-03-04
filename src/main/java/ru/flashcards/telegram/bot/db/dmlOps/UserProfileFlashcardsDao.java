package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class UserProfileFlashcardsDao {
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

    /**
     * Добавить карточку для изучения
     */
    public int addUserFlashcard(String word, String description, String transcription, String translation, Long categoryId, Long chatId) {
        return dsl.insertInto(USER_FLASHCARD)
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

    /**
     * Исключить карточку
     */
    public int exceptFlashcard(Long chatId, Long flashcardId) {
        return dsl.insertInto(EXCEPTED_USER_FLASHCARD)
                .columns(
                        EXCEPTED_USER_FLASHCARD.USER_ID,
                        EXCEPTED_USER_FLASHCARD.FLASHCARD_ID
                )
                .select(
                        select(
                                select(USER.ID).from(USER).where(USER.CHAT_ID.eq(chatId)),
                                val(flashcardId)
                        )
                )
                .execute();
    }
}
