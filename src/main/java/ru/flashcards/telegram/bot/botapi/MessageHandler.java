package ru.flashcards.telegram.bot.botapi;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler {
    List<BotApiMethod<?>> handle(Message message);
}
