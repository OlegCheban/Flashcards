package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.codegen.maven.flashcards.Sequences.COMMON_SEQ;
import static org.jooq.codegen.maven.flashcards.Tables.*;
import static org.jooq.impl.DSL.*;

@Component
@RequiredArgsConstructor
public class UsersDao {

    private final DSLContext dsl;

    @Transactional
    public void registerUser(Long chatId, String username) {
        final int randomNotificationInterval = 60;

        // Insert user if not exists
        dsl.insertInto(USER)
            .columns(USER.ID, USER.NAME, USER.NOTIFICATION_INTERVAL, USER.CHAT_ID)
            .select(
                select(
                    COMMON_SEQ.nextval(),
                    val(username),
                    val(randomNotificationInterval),
                    val(chatId)
                )
                .whereNotExists(
                    selectOne()
                       .from(USER)
                       .where(USER.CHAT_ID.eq(chatId))
                )
            )
            .execute();

        // Insert user exercise settings
        var usr = USER.as("usr");
        var a = LEARNING_EXERCISE_KIND.as("a");
        var s = USER_EXERCISE_SETTINGS.as("s");

        dsl.insertInto(USER_EXERCISE_SETTINGS)
            .columns(
                USER_EXERCISE_SETTINGS.ID,
                USER_EXERCISE_SETTINGS.USER_ID,
                USER_EXERCISE_SETTINGS.EXERCISE_KIND_ID
            )
            .select(
                select(
                    COMMON_SEQ.nextval(),
                    field(
                        select(usr.ID)
                           .from(usr)
                           .where(usr.CHAT_ID.eq(chatId))
                    ),
                    a.ID
                )
                .from(a)
                .whereNotExists(
                    selectOne()
                       .from(s)
                       .where(s.EXERCISE_KIND_ID.eq(a.ID))
                       .and(s.USER_ID.eq(
                                select(usr.ID)
                                   .from(usr)
                                   .where(usr.CHAT_ID.eq(chatId))
                        ))
                )
            )
            .execute();
    }
}
