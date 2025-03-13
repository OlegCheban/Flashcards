package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.UsersDao;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class UserDaoTest {
    @Autowired
    private UsersDao usersDao;

    @Test
    void registerUserTest(){
        usersDao.registerUser(999L, "Test");
    }
}
