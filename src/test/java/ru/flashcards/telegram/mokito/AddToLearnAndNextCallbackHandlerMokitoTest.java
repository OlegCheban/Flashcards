package ru.flashcards.telegram.mokito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ADD_NEXT;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
@Tag("mockito")
public class AddToLearnAndNextCallbackHandlerMokitoTest {
    private static final long CHAT_ID = 1L;
    private static final long FLASHCARD_ID = 0L;

    @Mock
    private CallbackQuery callbackQuery;
    @Mock
    private Message message;
    @Mock
    private SuggestFlashcard suggestFlashcard;
    @Mock
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    @Mock
    private FlashcardsDao flashcardsDao;

    private final Flashcard flashcard = new Flashcard(1L, "description", "transcription", "translation", "word");

    @BeforeEach
    public void setUp() {
        when(message.getMessageId()).thenReturn(0);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(flashcardsDao.findFlashcardById(FLASHCARD_ID)).thenReturn(Optional.of(flashcard));
        when(userProfileFlashcardsDao.addUserFlashcardIfAbsent(CHAT_ID, flashcard)).thenReturn(true);
    }

    @Test
    public void shouldReturnMessageAddToLearnAndNextCallbackHandlerTest() {
        AddToLearnAndNextCallbackHandler handler = new AddToLearnAndNextCallbackHandler(
                new AddToLearnCallbackHandler(userProfileFlashcardsDao, flashcardsDao), suggestFlashcard);

        List<BotApiMethod<?>> list = handler.handle(callbackQuery, new CallbackData(ADD_NEXT, FLASHCARD_ID));

        assertEquals("Карточка *word* добавлена для изучения", ((EditMessageText) list.get(0)).getText());
    }
}
