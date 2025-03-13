package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class LearningExercisesDaoTest {

    final long chatId = 256058999L;

    @Autowired
    private LearningExercisesDao learningExercisesDao;

    @Test
    void findCurrentExerciseCardTest(){
        var res = learningExercisesDao.findCurrentExerciseCard(chatId);
        assertNotNull(res);
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

    @Test
    void getFlashcardsByWordToSuggestLearningTest(){
        var res = learningExercisesDao.getFlashcardsByWordToSuggestLearning(chatId, "ingest").size();
        assertTrue(res >= 0);
    }
}
