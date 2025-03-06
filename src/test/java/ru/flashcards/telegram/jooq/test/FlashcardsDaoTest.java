package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class FlashcardsDaoTest {

    final long chatId = 256058999L;

    @Autowired
    private FlashcardsDao flashcardsDao;

    @Test
    void getFlashcardsByCategoryToSuggestLearningTest() {
        Long categoryId = 317L;
        var resWithCategory = flashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, categoryId);
        assertNotNull(resWithCategory);
        assertTrue(resWithCategory.size() >= 0);
    }
}
