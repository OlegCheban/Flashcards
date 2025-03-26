package ru.flashcards.telegram.bot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.flashcards.telegram.bot.FlashcardBot;
import ru.flashcards.telegram.bot.botapi.*;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.exception.JsonProcessingRuntimeException;

import java.util.List;

import static ru.flashcards.telegram.bot.botapi.MessageFactoryType.*;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebhookController {
    private static Logger logger = LoggerFactory.getLogger(WebhookController.class);
    final FlashcardBot flashcardBot;
    MessageFactoryProvider messageFactoryProvider;
    CallbackFactoryProvider callbackFactoryProvider;
    UserModeSettings userModeSettings;

    @PostMapping("/webhook")
    public void onUpdateReceived(@RequestBody Update update) {
        try {
            if (update.hasMessage()){
                execute(handleMessageInput(update.getMessage()));
            } else if (update.hasCallbackQuery()) {
                execute(handleCallbackQueryInput(update.getCallbackQuery()));
            }
        } catch (Exception e){
            logger.error("Internal error", e);
        }
    }
    @GetMapping("/test")
    public ResponseEntity<String> test(Update update) {
        return ResponseEntity.status(HttpStatus.OK).body("1.2.4");
    }

    private List<BotApiMethod<?>> handleMessageInput(Message message) {
        MessageHandlerAbstractFactory factory = getFactory(message);
        MessageHandler<Message> handler = (MessageHandler<Message>) factory.getHandler(message);
        return handler.handle(message);
    }

    private MessageHandlerAbstractFactory getFactory(Message message) {
        long chatId = message.getChatId();
        if (userModeSettings.getMode(chatId) == UserMode.EXERCISE) {
            return messageFactoryProvider.getFactory(EXERCISE);
        } else if (userModeSettings.getMode(chatId) == UserMode.WATERING_SESSION) {
            return messageFactoryProvider.getFactory(WATERING_SESSION);
        } else if (userModeSettings.getMode(chatId) == UserMode.PREPOSITION) {
            return messageFactoryProvider.getFactory(PREPOSITION);
        } else {
            return messageFactoryProvider.getFactory(OTHER_MESSAGES);
        }
    }

    private List<BotApiMethod<?>> handleCallbackQueryInput(CallbackQuery callbackQuery) {
        CallbackData callbackData = getCallbackData(callbackQuery);
        MessageHandler<CallbackQuery> handler = getCallbackHandler(callbackData);
        return handler.handle(callbackQuery);
    }

    private CallbackData getCallbackData(CallbackQuery callbackQuery) {
        try {
            return new ObjectMapper().readValue(callbackQuery.getData(), CallbackData.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException(e);
        }
    }

    private MessageHandler<CallbackQuery> getCallbackHandler(CallbackData callbackData) {
        CallbackFactory factory = (CallbackFactory) callbackFactoryProvider.getFactory(CALLBACK);
        return factory.getHandler(callbackData);
    }

    private void execute(List<BotApiMethod<?>> response){
        response.forEach(messageAnswer -> {
            try {
                flashcardBot.execute(messageAnswer);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}