package ru.flashcards.telegram.mokito;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ADD_NEXT;

@ExtendWith(MockitoExtension.class)
@Tag("mockito")
public class AddToLearnAndNextCallbackHandlerMokitoTest {
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
    @Mock
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
        AddToLearnAndNextCallbackHandler handler = new AddToLearnAndNextCallbackHandler(userProfileFlashcardsDao, suggestFlashcard, flashcardsDao);
        List<BotApiMethod<?>> list = handler.handle(callbackQuery);
        assertEquals("Карточка *word* добавлена для изучения", ((EditMessageText) list.get(0)).getText());
    }
}
