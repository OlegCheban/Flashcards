package ru.flashcards.telegram.bot.db;

import static org.jooq.codegen.maven.flashcards.Tables.SWIPER_FLASHCARDS;
import static org.jooq.impl.DSL.cast;
import static org.jooq.impl.DSL.lag;
import static org.jooq.impl.DSL.lead;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.noCondition;
import static org.jooq.impl.DSL.select;

import java.util.Objects;
import java.util.Optional;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.codegen.maven.flashcards.tables.SwiperFlashcards;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;

/**
 * The Swiper browses a user's flashcards ordered by id.
 * Filters: {@code characterCondition} — word prefix, {@code percentile} — exact learning percentage;
 * empty means no filter.
 */
@Component
@RequiredArgsConstructor
public class SwiperDao {

    private final DSLContext dsl;

    public Optional<Long> getFirstSwiperFlashcard(Long chatId, String characterCondition, String percentile) {
        return Optional.ofNullable(
                dsl.select(min(SWIPER_FLASHCARDS.ID))
                        .from(SWIPER_FLASHCARDS)
                        .where(SWIPER_FLASHCARDS.CHAT_ID.eq(chatId))
                        .and(matches(SWIPER_FLASHCARDS, characterCondition, percentile))
                        .fetchOne(0, Long.class));
    }

    public Optional<SwiperFlashcard> getSwiperFlashcard(
            Long chatId, Long currentFlashcardId, String characterCondition, String percentile) {
        var sf = SWIPER_FLASHCARDS.as("sf");
        Field<Long> prevId = lag(sf.ID).over().orderBy(sf.ID).as("prev_id");
        Field<Long> nextId = lead(sf.ID).over().orderBy(sf.ID).as("next_id");

        var window = select(
                    prevId,
                    sf.ID,
                    nextId,
                    sf.WORD,
                    sf.DESCRIPTION,
                    sf.TRANSLATION,
                    sf.TRANSCRIPTION,
                    sf.PRC,
                    sf.NEAREST_TRAINING)
                .from(sf)
                .where(sf.CHAT_ID.eq(chatId))
                .and(matches(sf, characterCondition, percentile))
                .asTable("w");

        return dsl.select(
                    window.field(prevId),
                    window.field(nextId),
                    window.field(sf.ID),
                    window.field(sf.WORD),
                    window.field(sf.DESCRIPTION),
                    window.field(sf.TRANSLATION),
                    window.field(sf.TRANSCRIPTION),
                    window.field(sf.PRC),
                    window.field(sf.NEAREST_TRAINING))
                .from(window)
                .where(window.field(sf.ID).eq(currentFlashcardId))
                .fetchOptional(record -> new SwiperFlashcard(
                    record.value1(),
                    record.value2(),
                    record.value3(),
                    record.value4(),
                    record.value5(),
                    record.value6(),
                    record.value7(),
                    Objects.requireNonNullElse(record.value8(), 0L).intValue(),
                    Objects.requireNonNullElse(record.value9(), 0)
                ));
    }

    private Condition matches(SwiperFlashcards sf, String characterCondition, String percentile) {
        Condition condition = noCondition();
        if (characterCondition != null && !characterCondition.isEmpty()) {
            condition = condition.and(sf.WORD.startsWithIgnoreCase(characterCondition));
        }
        if (percentile != null && !percentile.isEmpty()) {
            condition = condition.and(cast(sf.PRC, String.class).eq(percentile));
        }
        return condition;
    }
}
