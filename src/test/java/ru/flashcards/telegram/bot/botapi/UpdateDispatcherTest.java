package ru.flashcards.telegram.bot.botapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.services.SendMessageService;

class UpdateDispatcherTest {
    private final MessageFactory messageFactory = mock(MessageFactory.class);
    private final CallbackFactory callbackFactory = mock(CallbackFactory.class);
    private final CallbackDataCodec callbackDataCodec = new CallbackDataCodec(new ObjectMapper());
    private final SendMessageService sendMessageService = mock(SendMessageService.class);
    private final UpdateDispatcher dispatcher = new UpdateDispatcher(
            messageFactory,
            mock(ExerciseMessageFactory.class),
            mock(WateringSessionMessageFactory.class),
            mock(PrepositionMessageFactory.class),
            mock(PhrasalVerbMessageFactory.class),
            callbackFactory,
            callbackDataCodec,
            new UserModeSettings(),
            sendMessageService);

    @Test
    void ignoresMessagesWithoutText() {
        Message sticker = mock(Message.class);
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(sticker);

        assertTrue(dispatcher.dispatch(update).isEmpty());
        verifyNoInteractions(messageFactory);
    }

    @Test
    void answersCallbackQueryBeforeHandlerRuns() {
        CallbackData callbackData = new CallbackData(BotKeyboardButton.QUITE, 1L);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(callbackQuery.getId()).thenReturn("query-id");
        when(callbackQuery.getData()).thenReturn(callbackDataCodec.encode(callbackData));
        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        BotApiMethod<?> reply = mock(BotApiMethod.class);
        CallbackHandler handler = (query, data) -> {
            verify(sendMessageService).send(AnswerCallbackQuery.builder().callbackQueryId("query-id").build());
            return List.of(reply);
        };
        when(callbackFactory.getHandler(callbackData)).thenReturn(handler);

        List<BotApiMethod<?>> replies = dispatcher.dispatch(update);

        assertEquals(List.of(reply), replies);
    }
}
