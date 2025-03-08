package ru.flashcards.telegram.component.botapi.handlers.learn;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.Flashcard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ADD_NEXT;

//@SpringBootTest(classes = AddToLearnTestsConfiguration.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AddToLearnTestsConfiguration.class)
//@MockBean(SuggestFlashcard.class)
public class AddToLearnAndNextCallbackHandlerTest {
    @MockBean
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    @MockBean
    private SuggestFlashcard suggestFlashcard;
    @MockBean
    private FlashcardsDao flashcardsDao;
    @Autowired
    private AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler;
    @Autowired
    private AddToLearnCallbackHandler addToLearnCallbackHandler;
    @MockBean
    private CallbackQuery callbackQuery;
    @MockBean
    private Message message;
    @MockBean
    private Flashcard flashcard;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        CallbackData callbackData = new CallbackData(ADD_NEXT, 0L, new SwiperParams("",""));
        when(message.getMessageId()).thenReturn(0);
        when(callbackQuery.getData()).thenReturn(objectMapper.writeValueAsString(callbackData));
        when(callbackQuery.getMessage()).thenReturn(message);
        when(flashcard.word()).thenReturn("word");
        when(flashcardsDao.findFlashcardById(0L)).thenReturn(flashcard);
    }

    @Test
    public void shouldReturnMessageAddToLearnAndNextCallbackHandlerTest() {
        List<BotApiMethod<?>> list = addToLearnAndNextCallbackHandler.handle(callbackQuery);
        assertEquals("Карточка *word* добавлена для изучения", ((EditMessageText) list.get(0)).getText());
    }

    @Test
    public void shouldReturnMessageAddToLearnCallbackHandlerTest() {

        List<BotApiMethod<?>> list = addToLearnCallbackHandler.handle(callbackQuery);
        assertEquals("Карточка *word* добавлена для изучения", ((EditMessageText) list.get(0)).getText());
    }
}
