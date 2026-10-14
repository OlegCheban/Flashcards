package ru.flashcards.telegram.bot.botapi;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import ru.flashcards.telegram.bot.botapi.records.CallbackData;

public interface CallbackHandler {
    List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData);
}
