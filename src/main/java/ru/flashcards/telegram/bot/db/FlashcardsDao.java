package ru.flashcards.telegram.bot.db;

import static org.jooq.codegen.maven.flashcards.Tables.EXCEPTED_USER_FLASHCARD;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARD;
import static org.jooq.codegen.maven.flashcards.Tables.FLASHCARD_EXAMPLES;
import static org.jooq.codegen.maven.flashcards.Tables.RANDOM_FLASHCARD;
import static org.jooq.codegen.maven.flashcards.Tables.USER;
import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
import static org.jooq.impl.DSL.concat;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.noCondition;
import static org.jooq.impl.DSL.rowNumber;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.val;

import java.util.List;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.db.dto.SendToLearnFlashcard;

@Component
@RequiredArgsConstructor
public class FlashcardsDao {

    private final DSLContext dsl;

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

    public Optional<Flashcard> findFlashcardById(Long flashcardId) {
        return dsl.select(
                    FLASHCARD.CATEGORY_ID,
                    FLASHCARD.DESCRIPTION,
                    FLASHCARD.TRANSCRIPTION,
                    FLASHCARD.TRANSLATION,
                    FLASHCARD.WORD)
                .from(FLASHCARD)
                .where(FLASHCARD.ID.eq(flashcardId))
                .fetchOptional(Records.mapping(Flashcard::new));
    }

    /**
     * A flashcard of the category that the user has neither added nor excluded.
     *
     * @param flashcardCategoryId category filter, {@code null} means any category
     */
    public List<SendToLearnFlashcard> getFlashcardsByCategoryToSuggestLearning(Long chatId, Long flashcardCategoryId) {
        var u = USER.as("u");
        var f = FLASHCARD.as("f");
        var euf = EXCEPTED_USER_FLASHCARD.as("euf");
        var uf = USER_FLASHCARD.as("uf");

        Condition categoryCondition = flashcardCategoryId == null
                ? noCondition()
                : f.CATEGORY_ID.eq(flashcardCategoryId);
        var fc = lateral(
                select(f.ID, f.WORD, f.DESCRIPTION, f.TRANSLATION, f.TRANSCRIPTION)
                        .from(f)
                        .whereNotExists(
                                selectOne()
                                        .from(euf)
                                        .where(euf.FLASHCARD_ID.eq(f.ID))
                                        .and(euf.USER_ID.eq(u.ID)))
                        .andNotExists(
                                selectOne()
                                        .from(uf)
                                        .where(uf.WORD.eq(f.WORD))
                                        .and(uf.USER_ID.eq(u.ID)))
                        .and(categoryCondition)
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

    public List<String> getRandomTranslations() {
        return dsl.select(RANDOM_FLASHCARD.TRANSLATION)
                .from(RANDOM_FLASHCARD)
                .fetch(RANDOM_FLASHCARD.TRANSLATION);
    }

    public List<String> getRandomWords() {
        return dsl.select(RANDOM_FLASHCARD.WORD)
                .from(RANDOM_FLASHCARD)
                .fetch(RANDOM_FLASHCARD.WORD);
    }

    public List<String> getRandomDescriptions() {
        return dsl.select(RANDOM_FLASHCARD.DESCRIPTION)
                .from(RANDOM_FLASHCARD)
                .fetch(RANDOM_FLASHCARD.DESCRIPTION);
    }
}
