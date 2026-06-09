package ru.flashcards.telegram.bot.botapi.handlers.pronunciation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;
import ru.flashcards.telegram.bot.services.PronunciationService;
import ru.flashcards.telegram.bot.services.SendMessageService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class PronunciationCallbackHandler implements MessageHandler<CallbackQuery> {
    private FlashcardsDao flashcardsDao;
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    private PronunciationService pronunciationService;
    private SendMessageService sendMessageService;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        Long chatId = callbackQuery.getMessage().getChatId();

        String word = resolveWord(callbackData);
        if (word == null) {
            return List.of(message(chatId, "Карточка не найдена."));
        }

        Optional<String> audioUrl = pronunciationService.findAudioUrl(word);
        if (audioUrl.isEmpty()) {
            return List.of(message(chatId, "Произношение для слова *" + word + "* не найдено."));
        }

        // The audio is sent directly via the Telegram HTTP API (Telegram downloads the URL),
        // mirroring SuggestFlashcard, so no BotApiMethod is returned for it.
        sendMessageService.sendAudio(chatId, audioUrl.get());
        return Collections.emptyList();
    }

    /**
     * SPRON carries a USER_FLASHCARD id (Swiper, notifications); PRON carries a FLASHCARD id
     * (the suggest-a-card menu). Resolve the word from the matching table.
     */
    private String resolveWord(CallbackData callbackData) {
        if (callbackData.command() == BotKeyboardButton.SPRON) {
            UserFlashcard userFlashcard = userProfileFlashcardsDao.findUserFlashcardById(callbackData.entityId());
            return userFlashcard == null ? null : userFlashcard.word();
        }
        Flashcard flashcard = flashcardsDao.findFlashcardById(callbackData.entityId());
        return flashcard == null ? null : flashcard.word();
    }

    private SendMessage message(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode("Markdown");
        sendMessage.setText(text);
        return sendMessage;
    }
}
