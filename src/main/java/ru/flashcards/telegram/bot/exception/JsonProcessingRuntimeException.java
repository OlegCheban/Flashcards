package ru.flashcards.telegram.bot.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Runtime-wrapper for exception {@link JsonProcessingException}
 */
public class JsonProcessingRuntimeException extends RuntimeException {
    public JsonProcessingRuntimeException(JsonProcessingException cause){
        super(cause);
    }
}
