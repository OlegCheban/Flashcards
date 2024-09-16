package ru.flashcards.telegram.bot.botapi;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandlerAbstractFactory<T> {
    T getHandler(Message message);
}
