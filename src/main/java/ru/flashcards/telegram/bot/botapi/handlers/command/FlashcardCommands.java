package ru.flashcards.telegram.bot.botapi.handlers.command;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.MAKEUP;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.MessageType;
import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.UserMessageTypeBuffer;
import ru.flashcards.telegram.bot.botapi.handlers.context.FlashcardsContextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;
import ru.flashcards.telegram.bot.utils.Numbers;

/**
 * Commands /f, /ed and /rl.
 */
@Component
@RequiredArgsConstructor
public class FlashcardCommands {
    private final SuggestFlashcard suggestFlashcard;
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final LearningExercisesDao learningExercisesDao;
    private final UserMessageTypeBuffer userMessageTypeBuffer;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    /**
     * Without a word suggests the next popular flashcard.
     */
    public List<BotApiMethod<?>> find(Long chatId, String word) {
        if (word.isEmpty()) {
            return suggestFlashcard.byTop3000Category(chatId);
        }
        return suggestFlashcard.byParam(chatId, word);
    }

    /**
     * The new translation is expected in the next message.
     */
    public List<BotApiMethod<?>> changeTranslation(Long chatId, String word) {
        Optional<UserFlashcard> flashcard = userProfileFlashcardsDao.findUserFlashcardByName(chatId, word);
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.text(chatId, "Карточка не найдена в Вашем профиле"));
        }

        userMessageTypeBuffer.putRequest(chatId, flashcard.get().id(), MessageType.CHANGE_TRANSLATION);
        return List.of(BotMessages.text(chatId, "Отправьте перевод"));
    }

    public List<BotApiMethod<?>> recentLearned(Long chatId, String quantityArgument) {
        OptionalInt quantity = Numbers.parsePositiveInt(quantityArgument);
        if (quantity.isEmpty() || quantity.getAsInt() > FlashcardsContextCallbackHandler.MAX_WORDS) {
            return List.of(BotMessages.text(chatId,
                    "Неверный параметр, должно быть число от 1 до " + FlashcardsContextCallbackHandler.MAX_WORDS));
        }

        long wordsQuantity = quantity.getAsInt();
        List<String> recentLearned = learningExercisesDao.getRecentLearned(chatId, wordsQuantity, true);
        StringBuilder text = new StringBuilder("Последние изученные карточки:\n");
        for (int i = 0; i < recentLearned.size(); i++) {
            text.append(i + 1).append(". ").append(recentLearned.get(i)).append('\n');
        }
        text.append("\nПродолжайте учить!");

        SendMessage message = BotMessages.markdown(chatId, text.toString());
        message.setReplyMarkup(inlineKeyboardFactory.row(List.of(
                inlineKeyboardFactory.button("Создать контекст", new CallbackData(MAKEUP, wordsQuantity)))));
        return List.of(message);
    }
}
