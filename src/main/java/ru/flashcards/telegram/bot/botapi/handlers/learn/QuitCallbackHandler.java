package ru.flashcards.telegram.bot.botapi.handlers.learn;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.DONE_MSG;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;

@Component
public class QuitCallbackHandler implements CallbackHandler {

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        return List.of(BotMessages.editText(callbackQuery.getMessage(), DONE_MSG));
    }
}
