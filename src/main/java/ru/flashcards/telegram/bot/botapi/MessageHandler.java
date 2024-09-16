package ru.flashcards.telegram.bot.botapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.exception.JsonProcessingRuntimeException;

import java.util.List;

public interface MessageHandler<T extends BotApiObject> {
    List<BotApiMethod<?>> handle(T message);

    default CallbackData jsonToCallbackData(String callbackDataJson){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(callbackDataJson, CallbackData.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException(e);
        }
    }

    default String callbackDataToJson(CallbackData callbackData){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(callbackData);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException(e);
        }
    }
}
