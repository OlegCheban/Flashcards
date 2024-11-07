package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcards;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class UserProfileFlashcardsTest {
    @Autowired
    private UserProfileFlashcards userProfileFlashcards;
    @Test
    void test(){
        var res = userProfileFlashcards.findUnlearnedFlashcardKeyword(256058999L, 4).size();
        int high = 4;
        int low = 0;
        assertTrue(high >= res, "Error, res is too high");
        assertTrue(low  <= res, "Error, res is too low");
    }
}
