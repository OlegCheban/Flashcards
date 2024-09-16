package ru.flashcards.telegram.bot.exception;

import java.sql.SQLException;

/**
 * Runtime-wrapper for exception {@link SQLException}
 */
public class SQLRuntimeException extends RuntimeException {
    public SQLRuntimeException (SQLException cause){
        super(cause);
    }
}
