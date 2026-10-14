package ru.flashcards.telegram.bot.botapi.handlers.learn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;

/**
 * Adds the flashcard to the profile and suggests the next one.
 */
@Component
@RequiredArgsConstructor
public class AddToLearnAndNextCallbackHandler implements CallbackHandler {
    private final AddToLearnCallbackHandler addToLearnCallbackHandler;
    private final SuggestFlashcard suggestFlashcard;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        List<BotApiMethod<?>> replies = new ArrayList<>(addToLearnCallbackHandler.handle(callbackQuery, callbackData));
        replies.addAll(suggestFlashcard.byTop3000Category(callbackQuery.getMessage().getChatId()));
        return replies;
    }
}
