package ru.flashcards.telegram.bot.exception;

/**
 * The pronunciation audio cannot be produced: the speech synthesis API failed or its audio cannot be converted.
 */
public class PronunciationUnavailableException extends RuntimeException {
    public PronunciationUnavailableException(String message) {
        super(message);
    }

    public PronunciationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
