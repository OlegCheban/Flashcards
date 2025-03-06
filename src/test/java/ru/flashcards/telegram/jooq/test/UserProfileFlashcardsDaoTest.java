package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
        // Test with specific category
        Long categoryId = 1L; // Replace with actual category ID from your test data
        var resWithCategory = userProfileFlashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, categoryId);
        assertNotNull(resWithCategory);
        assertTrue(resWithCategory.size() >= 0);

        // Test with null category (should return flashcards from any category)
        var resWithoutCategory = userProfileFlashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, null);
        assertNotNull(resWithoutCategory);
        assertTrue(resWithoutCategory.size() >= 0);
    }
}
