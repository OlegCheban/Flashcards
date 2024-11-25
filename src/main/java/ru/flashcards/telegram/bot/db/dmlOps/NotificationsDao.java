package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static org.jooq.codegen.maven.flashcards.Tables.USER_FLASHCARD;
import static org.jooq.impl.DSL.currentLocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationsDao {
    private final DSLContext dsl;

    public void updatePushTimestamp(Long flashcardId){
        dsl.update(USER_FLASHCARD)
                .set(USER_FLASHCARD.PUSH_TIMESTAMP, currentLocalDateTime())
                .where(USER_FLASHCARD.ID.eq(flashcardId))
                .execute();
    }
}
