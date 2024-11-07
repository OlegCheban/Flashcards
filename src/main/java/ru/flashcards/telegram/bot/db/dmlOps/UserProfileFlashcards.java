package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Tables.USER;
import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
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
}
