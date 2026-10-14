package ru.flashcards.telegram.bot.botapi;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.exception.JsonProcessingRuntimeException;

/**
 * Converts {@link CallbackData} to and from the compact JSON stored in Telegram's callback_data.
 */
@Component
@RequiredArgsConstructor
public class CallbackDataCodec {
    private final ObjectMapper objectMapper;

    public CallbackData decode(String callbackDataJson) {
        try {
            return objectMapper.readValue(callbackDataJson, CallbackData.class);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException("Invalid callback data: " + callbackDataJson, e);
        }
    }

    public String encode(CallbackData callbackData) {
        try {
            return objectMapper.writeValueAsString(callbackData);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException("Unable to serialize callback data: " + callbackData, e);
        }
    }
}
