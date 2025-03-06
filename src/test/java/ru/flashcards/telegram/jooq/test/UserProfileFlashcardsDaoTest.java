package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class UserProfileFlashcardsDaoTest {

    final long chatId = 256058999L;

    @Autowired
    private UserProfileFlashcardsDao userProfileFlashcardsDao;

    @Test
    void findUnlearnedFlashcardKeywordTest(){
        var res = userProfileFlashcardsDao.findUnlearnedFlashcardKeyword(chatId, 4).size();
        assertTrue(res > 0);
    }

    @Test
    void findUserCardsForTrainingTest(){
        var res = userProfileFlashcardsDao.findUserCardsForTraining(chatId).size();
        assertTrue(res > 0);
    }

    @Test
    void findLearnedFlashcardsTest(){
        var res = userProfileFlashcardsDao.findLearnedFlashcards(chatId).size();
        assertTrue(res >= 0);
    }
}
