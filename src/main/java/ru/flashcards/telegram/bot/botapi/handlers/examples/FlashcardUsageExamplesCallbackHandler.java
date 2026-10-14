package ru.flashcards.telegram.bot.botapi.handlers.examples;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;

@Component
@RequiredArgsConstructor
public class FlashcardUsageExamplesCallbackHandler implements CallbackHandler {
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        Long chatId = callbackQuery.getMessage().getChatId();
        List<String> examples = userProfileFlashcardsDao.getExamplesByUserFlashcardId(callbackData.entityId());
        if (examples.isEmpty()) {
            return List.of(BotMessages.text(chatId, "Для данной карточки нет примеров."));
        }

        List<BotApiMethod<?>> replies = new ArrayList<>();
        for (String example : examples) {
            replies.add(BotMessages.markdown(chatId, example));
        }
        return replies;
    }
}
