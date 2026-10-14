package ru.flashcards.telegram.bot.botapi;

import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.services.SendMessageService;

/**
 * Routes an incoming update to the handler that matches the user's current mode or the pressed button.
 */
@Service
@RequiredArgsConstructor
public class UpdateDispatcher {
    private final MessageFactory messageFactory;
    private final ExerciseMessageFactory exerciseMessageFactory;
    private final WateringSessionMessageFactory wateringSessionMessageFactory;
    private final PrepositionMessageFactory prepositionMessageFactory;
    private final PhrasalVerbMessageFactory phrasalVerbMessageFactory;
    private final CallbackFactory callbackFactory;
    private final CallbackDataCodec callbackDataCodec;
    private final UserModeSettings userModeSettings;
    private final SendMessageService sendMessageService;

    public List<BotApiMethod<?>> dispatch(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return handleMessage(update.getMessage());
        }
        if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update.getCallbackQuery());
        }
        return List.of();
    }

    private List<BotApiMethod<?>> handleMessage(Message message) {
        return factoryFor(message.getChatId()).getHandler(message).handle(message);
    }

    private MessageHandlerAbstractFactory factoryFor(Long chatId) {
        UserMode userMode = userModeSettings.getMode(chatId);
        if (userMode == null) {
            return messageFactory;
        }
        return switch (userMode) {
            case EXERCISE -> exerciseMessageFactory;
            case WATERING_SESSION -> wateringSessionMessageFactory;
            case PREPOSITION -> prepositionMessageFactory;
            case PHRASAL_VERB -> phrasalVerbMessageFactory;
        };
    }

    /**
     * The callback query is answered right away, before a possibly slow handler (e.g. AI generation) runs:
     * the client stops showing the progress indicator, and Telegram rejects answers to old queries.
     */
    private List<BotApiMethod<?>> handleCallbackQuery(CallbackQuery callbackQuery) {
        sendMessageService.send(AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId()).build());

        CallbackData callbackData = callbackDataCodec.decode(callbackQuery.getData());
        return callbackFactory.getHandler(callbackData).handle(callbackQuery, callbackData);
    }
}
