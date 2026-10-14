package ru.flashcards.telegram.bot.botapi.handlers.context;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.services.FlashcardsContextService;

/**
 * "Создать контекст": an AI generated story with the recently learned words.
 * The callback carries the number of recent words.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashcardsContextCallbackHandler implements CallbackHandler {
    public static final long MAX_WORDS = 100;

    private final LearningExercisesDao learningExercisesDao;
    private final FlashcardsContextService flashcardsContextService;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Long requestedQuantity = callbackData.entityId();
        long wordsQuantity = requestedQuantity == null
                ? MAX_WORDS
                : Math.max(1, Math.min(requestedQuantity, MAX_WORDS));
        List<String> words = learningExercisesDao.getRecentLearned(chatId, wordsQuantity, false);
        if (words.isEmpty()) {
            return List.of(BotMessages.text(chatId, "Нет выученных карточек"));
        }

        try {
            String story = flashcardsContextService.generateContext(String.join(", ", words));
            return List.of(BotMessages.markdown(chatId, story));
        } catch (RuntimeException e) {
            log.error("AI context generation failed: chatId={}", chatId, e);
            return List.of(BotMessages.text(chatId, "Не удалось сгенерировать контекст, попробуйте позже"));
        }
    }
}
