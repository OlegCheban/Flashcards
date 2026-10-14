package ru.flashcards.telegram.bot.botapi.handlers.pronunciation;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARD_NOT_FOUND_MSG;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;
import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;
import ru.flashcards.telegram.bot.services.PronunciationService;
import ru.flashcards.telegram.bot.services.SendMessageService;

@Component
@RequiredArgsConstructor
public class PronunciationCallbackHandler implements CallbackHandler {
    private final FlashcardsDao flashcardsDao;
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final PronunciationService pronunciationService;
    private final SendMessageService sendMessageService;

    /**
     * The audio is not a {@link BotApiMethod}, so it is sent directly and nothing is returned for it.
     */
    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        Long chatId = callbackQuery.getMessage().getChatId();

        Optional<String> word = resolveWord(chatId, callbackData);
        if (word.isEmpty()) {
            return List.of(BotMessages.text(chatId, FLASHCARD_NOT_FOUND_MSG));
        }

        Optional<String> audioUrl = pronunciationService.findAudioUrl(word.get());
        if (audioUrl.isEmpty()) {
            return List.of(BotMessages.markdown(chatId,
                    "Произношение для слова *" + BotMessages.escapeMarkdown(word.get()) + "* не найдено."));
        }

        sendMessageService.sendAudio(chatId, audioUrl.get());
        return List.of();
    }

    /**
     * SPRON carries a USER_FLASHCARD id (Swiper, notifications); PRON carries a FLASHCARD id
     * (the suggest-a-card menu). Resolve the word from the matching table.
     */
    private Optional<String> resolveWord(Long chatId, CallbackData callbackData) {
        if (callbackData.command() == BotKeyboardButton.SPRON) {
            return userProfileFlashcardsDao.findUserFlashcardById(chatId, callbackData.entityId())
                    .map(UserFlashcard::word);
        }
        return flashcardsDao.findFlashcardById(callbackData.entityId()).map(Flashcard::word);
    }
}
