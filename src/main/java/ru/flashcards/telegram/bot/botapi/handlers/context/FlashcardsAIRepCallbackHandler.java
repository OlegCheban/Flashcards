package ru.flashcards.telegram.bot.botapi.handlers.context;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARD_NOT_FOUND_MSG;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.services.FlashcardsContextService;
import ru.flashcards.telegram.bot.services.SendMessageService;

/**
 * "Узнать больше": an AI generated report about the word.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashcardsAIRepCallbackHandler implements CallbackHandler {
    private final FlashcardsDao flashcardsDao;
    private final FlashcardsContextService flashcardsContextService;
    private final SendMessageService sendMessageService;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Optional<Flashcard> flashcard = flashcardsDao.findFlashcardById(callbackData.entityId());
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.text(chatId, FLASHCARD_NOT_FOUND_MSG));
        }

        // generation takes a while, so the user is told about it right away
        sendMessageService.send(BotMessages.text(chatId, "ИИ генерирует отчет ..."));

        String report;
        try {
            report = flashcardsContextService.generateFlashcardReport(flashcard.get().word());
        } catch (RuntimeException e) {
            log.error("AI report generation failed: flashcardId={}", callbackData.entityId(), e);
            return List.of(BotMessages.text(chatId, "Не удалось сгенерировать отчет, попробуйте позже"));
        }

        // split before conversion, so that every part has balanced tags
        List<BotApiMethod<?>> replies = new ArrayList<>();
        for (String part : BotMessages.splitText(report)) {
            replies.add(BotMessages.html(chatId, BotMessages.markdownBoldToHtml(part)));
        }
        return replies;
    }
}
