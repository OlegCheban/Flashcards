package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class UserProfileFlashcardsDaoTest {
    final long chatId = 256058999L;
    @Autowired
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    @Autowired
    private LearningExercisesDao learningExercisesDao;
    @Autowired
    private FlashcardsDao flashcardsDao;

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

    @Test
    void findCurrentExerciseCardTest(){
        var res = learningExercisesDao.findCurrentExerciseCard(chatId);
        assertNotNull(res);
    }

    @Test
    void getFlashcardsByCategoryToSuggestLearningTest() {
        Long categoryId = 317L;
        var resWithCategory = flashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, categoryId);
        assertNotNull(resWithCategory);
        assertTrue(resWithCategory.size() >= 0);
    }

    @Test
    void getExerciseKindToEnableTest(){
        var res = learningExercisesDao.getExerciseKindToEnable(chatId).size();
        assertTrue(res >= 0);
    }

    @Test
    void getExerciseKindToDisableTest(){
        var res = learningExercisesDao.getExerciseKindToDisable(chatId).size();
        assertTrue(res >= 0);
    }

    @Test
    void existsExerciseTest(){
        var res = learningExercisesDao.existsExercise(chatId);
        assertTrue(res);
    }

    @Test
    void existsLearnedFlashcardsTest(){
        var res = learningExercisesDao.existsLearnedFlashcards(chatId);
        assertTrue(res);
    }

    @Test
    void getRecentLearnedTest(){
        var res = learningExercisesDao.getRecentLearned(chatId, 10L).size();
        assertTrue(res >= 0);
    }
}
