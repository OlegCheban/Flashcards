package ru.flashcards.telegram.bot.exception;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Runtime-wrapper for exception {@link TelegramApiException}
 */
public class TelegramApiRuntimeException extends RuntimeException {
    public TelegramApiRuntimeException(TelegramApiException cause){
        super(cause);
    }
}
