package ru.flashcards.telegram.component.botapi.handlers.learn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ADD_NEXT;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AddToLearnTestsConfiguration.class)
public class AddToLearnAndNextCallbackHandlerTest {
    private static final long CHAT_ID = 1L;
    private static final long FLASHCARD_ID = 0L;

    @MockitoBean
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    @MockitoBean
    private SuggestFlashcard suggestFlashcard;
    @MockitoBean
    private FlashcardsDao flashcardsDao;
    @Autowired
    private AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler;
    @Autowired
    private AddToLearnCallbackHandler addToLearnCallbackHandler;

    private final CallbackQuery callbackQuery = mock(CallbackQuery.class);
    private final CallbackData callbackData = new CallbackData(ADD_NEXT, FLASHCARD_ID);
    private final Flashcard flashcard = new Flashcard(1L, "description", "transcription", "translation", "word");

    @BeforeEach
    public void setUp() {
        Message message = mock(Message.class);
        when(message.getMessageId()).thenReturn(0);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(flashcardsDao.findFlashcardById(FLASHCARD_ID)).thenReturn(Optional.of(flashcard));
        when(userProfileFlashcardsDao.addUserFlashcardIfAbsent(CHAT_ID, flashcard)).thenReturn(true);
    }

    @Test
    public void shouldReturnMessageAddToLearnAndNextCallbackHandlerTest() {
        List<BotApiMethod<?>> list = addToLearnAndNextCallbackHandler.handle(callbackQuery, callbackData);

        assertEquals("Карточка *word* добавлена для изучения", ((EditMessageText) list.get(0)).getText());
        verify(suggestFlashcard).byTop3000Category(CHAT_ID);
    }

    @Test
    public void shouldReturnMessageAddToLearnCallbackHandlerTest() {
        List<BotApiMethod<?>> list = addToLearnCallbackHandler.handle(callbackQuery, callbackData);

        assertEquals("Карточка *word* добавлена для изучения", ((EditMessageText) list.get(0)).getText());
    }

    @Test
    public void shouldReportAlreadyAddedFlashcard() {
        when(userProfileFlashcardsDao.addUserFlashcardIfAbsent(CHAT_ID, flashcard)).thenReturn(false);

        List<BotApiMethod<?>> list = addToLearnCallbackHandler.handle(callbackQuery, callbackData);

        assertEquals("Карточка *word* была ранее добавлена для изучения", ((EditMessageText) list.get(0)).getText());
    }
}
