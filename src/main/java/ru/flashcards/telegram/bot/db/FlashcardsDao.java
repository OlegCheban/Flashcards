package ru.flashcards.telegram.bot.db;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.db.dto.SendToLearnFlashcard;

import java.util.List;

import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class FlashcardsDao {
    private final DSLContext dsl;

    public void removeFlashcard(Long flashcardId) {
        dsl.deleteFrom(USER_FLASHCARD)
                .where(USER_FLASHCARD.ID.eq(flashcardId))
                .execute();
    }

    public List<String> getExamplesByFlashcardId(Long flashcardId) {
        return dsl.select(
                    concat(
                        rowNumber().over().orderBy(FLASHCARD_EXAMPLES.ID), 
                        val(". "), 
                        FLASHCARD_EXAMPLES.EXAMPLE
                    ).as("example"))
                .from(FLASHCARD_EXAMPLES)
                .where(FLASHCARD_EXAMPLES.FLASHCARD_ID.eq(flashcardId))
                .orderBy(FLASHCARD_EXAMPLES.ID)
                .fetchInto(String.class);
    }

    public Flashcard findFlashcardById(Long flashcardId) {
        return dsl.select(
                    FLASHCARD.CATEGORY_ID,
                    FLASHCARD.DESCRIPTION,
                    FLASHCARD.TRANSCRIPTION,
                    FLASHCARD.TRANSLATION,
                    FLASHCARD.WORD)
                .from(FLASHCARD)
                .where(FLASHCARD.ID.eq(flashcardId))
                .fetchOneInto(Flashcard.class);
    }

    public List<SendToLearnFlashcard> getFlashcardsByCategoryToSuggestLearning(Long chatId, Long flashcardCategoryId) {
        var u = USER.as("u");
        var f = FLASHCARD.as("f");
        var euf = EXCEPTED_USER_FLASHCARD.as("euf");
        var uf = USER_FLASHCARD.as("uf");

        return dsl.select(
                        u.CHAT_ID.as("chat_id"),
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
                                )
                                .from(f)
                                .whereNotExists(
                                        dsl.selectOne()
                                                .from(euf)
                                                .where(euf.FLASHCARD_ID.eq(f.ID))
                                                .and(euf.USER_ID.eq(u.ID))
                                )
                                .andNotExists(
                                        dsl.selectOne()
                                                .from(uf)
                                                .where(uf.WORD.eq(f.WORD))
                                                .and(uf.USER_ID.eq(u.ID))
                                )
                                .and(f.CATEGORY_ID.eq(coalesce(val(flashcardCategoryId), f.CATEGORY_ID)))
                                .limit(1)
                ).as("fc"))
                .on(trueCondition())
                .where(u.CHAT_ID.eq(chatId))
                .fetchInto(SendToLearnFlashcard.class);
    }
}
