package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcards;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.flashcards.telegram.bot.botapi.ExerciseKinds.MEMORISED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class UserProfileFlashcardsTest {
    final long chatId = 256058999L;
    @Autowired
    private UserProfileFlashcards userProfileFlashcards;

    @Test
    void findUnlearnedFlashcardKeywordTest(){
        var res = userProfileFlashcards.findUnlearnedFlashcardKeyword(chatId, 4).size();
        assertTrue(res > 0);
    }

    @Test
    void findUserCardsForTrainingTest(){
        var res = userProfileFlashcards.findUserCardsForTraining(chatId).size();
        assertTrue(res > 0);
    }

    @Test
    void findCurrentExerciseCardTest(){
        var res = userProfileFlashcards.findCurrentExerciseCard(chatId);
        assertNotNull(res);
    }
}
