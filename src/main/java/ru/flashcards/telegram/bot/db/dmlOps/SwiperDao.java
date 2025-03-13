package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.*;
import static org.jooq.codegen.maven.flashcards.Tables.*;

import org.jooq.Record;
import org.jooq.impl.DSL;
import ru.flashcards.telegram.bot.db.dmlOps.dto.SwiperFlashcard;

@Component
@RequiredArgsConstructor
public class SwiperDao {

    private final DSLContext dsl;

    public Long getFirstSwiperFlashcard(Long chatId, String characterCondition, String percentile) {
        return dsl.select(min(SWIPER_FLASHCARDS.ID))
                .from(SWIPER_FLASHCARDS)
                .where(SWIPER_FLASHCARDS.CHAT_ID.eq(chatId))
                .and(charLength(val(characterCondition)).eq(0)
                    .or(lower(SWIPER_FLASHCARDS.WORD).like(lower(val(characterCondition)).concat("%"))))
                .and(charLength(val(percentile)).eq(0)
                    .or(cast(SWIPER_FLASHCARDS.PRC, String.class).eq(percentile)))
                .fetchOneInto(Long.class);
    }

    public SwiperFlashcard getSwiperFlashcard(Long chatId, Long currentFlashcardId, String characterCondition, String percentile) {
        var sf = SWIPER_FLASHCARDS.as("sf");
        
        var subquery = dsl.select(
                    lag(sf.ID).over().orderBy(sf.ID).as("prev_id"),
                    sf.ID.as("current_id"),
                    lead(sf.ID).over().orderBy(sf.ID).as("next_id"),
                    sf.WORD,
                    sf.DESCRIPTION,
                    sf.TRANSLATION,
                    sf.TRANSCRIPTION,
                    sf.PRC,
                    sf.NEAREST_TRAINING
                )
                .from(sf)
                .where(sf.CHAT_ID.eq(chatId))
                .and(charLength(val(characterCondition)).eq(0)
                    .or(lower(sf.WORD).like(lower(val(characterCondition)).concat("%"))))
                .and(charLength(val(percentile)).eq(0)
                    .or(cast(sf.PRC, String.class).eq(percentile)))
                .orderBy(sf.ID);

        return dsl.select()
                .from(subquery)
                .where(field("current_id").eq(currentFlashcardId))
                .fetchOne(record -> new SwiperFlashcard(
                    record.get("prev_id", Long.class),
                    record.get("next_id", Long.class),
                    record.get("current_id", Long.class),
                    record.get(sf.WORD),
                    record.get(sf.DESCRIPTION),
                    record.get(sf.TRANSLATION),
                    record.get(sf.TRANSCRIPTION),
                    record.get(sf.PRC),
                    record.get(sf.NEAREST_TRAINING)
                ));
    }
}
