package ru.flashcards.telegram.jooq.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.flashcards.telegram.bot.db.dmlOps.SwiperDao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JooqTestConfig.class})
@Tag("jooq")
public class SwiperDaoTest {

    final long chatId = 256058999L;

    @Autowired
    private SwiperDao swiperDao;

    @Test
    void getFirstSwiperFlashcardTest(){
        var res = swiperDao.getFirstSwiperFlashcard(chatId, "", "");
        assertNotNull(res);
    }

    @Test
    void getSwiperFlashcardTest(){
        var res = swiperDao.getSwiperFlashcard(chatId, 96699L, "", "");
        assertNotNull(res);
    }
}
