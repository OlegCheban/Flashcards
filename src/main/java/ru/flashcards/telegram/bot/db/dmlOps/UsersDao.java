package ru.flashcards.telegram.bot.db.dmlOps;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsersDao {
    
    private final DSLContext dsl;
}
