package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
