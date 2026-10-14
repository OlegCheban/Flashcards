package ru.flashcards.telegram.bot.botapi;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ADD;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ADD_NEXT;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.AIREP;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXCL;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXCLN;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.PRON;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.QUITE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dto.SendToLearnFlashcard;

/**
 * Suggests flashcards from the common catalog to add to the user's profile.
 */
@Component
@RequiredArgsConstructor
public class SuggestFlashcard {
    private static final long TOP_3000_CATEGORY_ID = 713L;

    private final FlashcardsDao flashcardsDao;
    private final LearningExercisesDao learningExercisesDao;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    public List<BotApiMethod<?>> byParam(Long chatId, String word) {
        List<SendToLearnFlashcard> flashcards = learningExercisesDao.getFlashcardsByWordToSuggestLearning(chatId, word);
        if (flashcards.isEmpty()) {
            return List.of(BotMessages.markdown(chatId,
                    "Карточка *" + BotMessages.escapeMarkdown(word) + "* не найдена"));
        }
        return suggestions(flashcards, ADD, EXCL);
    }

    /**
     * Next flashcard of the "top 3000 words" category;
     * buttons of the suggestion show one more flashcard after a choice.
     */
    public List<BotApiMethod<?>> byTop3000Category(Long chatId) {
        return suggestions(flashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, TOP_3000_CATEGORY_ID),
                ADD_NEXT, EXCLN);
    }

    private List<BotApiMethod<?>> suggestions(
            List<SendToLearnFlashcard> flashcards, BotKeyboardButton addCommand, BotKeyboardButton excludeCommand) {
        List<BotApiMethod<?>> messages = new ArrayList<>();
        for (SendToLearnFlashcard flashcard : flashcards) {
            SendMessage message = BotMessages.markdown(flashcard.userId(), suggestionText(flashcard));
            message.setReplyMarkup(suggestionKeyboard(flashcard.flashcardId(), addCommand, excludeCommand));
            messages.add(message);
        }
        return messages;
    }

    private String suggestionText(SendToLearnFlashcard flashcard) {
        StringBuilder text = new StringBuilder()
                .append('*').append(flashcard.word()).append("* /").append(flashcard.transcription()).append("/\n")
                .append(flashcard.description())
                .append("\n\n*Перевод:* ").append(flashcard.translation()).append('\n');

        List<String> examples = flashcardsDao.getExamplesByFlashcardId(flashcard.flashcardId());
        if (!examples.isEmpty()) {
            text.append("*Примеры:*\n").append(String.join("\n", examples));
        }
        return text.toString();
    }

    private InlineKeyboardMarkup suggestionKeyboard(
            Long flashcardId, BotKeyboardButton addCommand, BotKeyboardButton excludeCommand) {
        return inlineKeyboardFactory.column(List.of(
                inlineKeyboardFactory.button("Добавить для изучения", new CallbackData(addCommand, flashcardId)),
                inlineKeyboardFactory.button("Узнать больше", new CallbackData(AIREP, flashcardId)),
                inlineKeyboardFactory.button("Слушать произношение", new CallbackData(PRON, flashcardId)),
                inlineKeyboardFactory.button("Знаю", new CallbackData(excludeCommand, flashcardId)),
                inlineKeyboardFactory.button("Выйти", new CallbackData(QUITE, flashcardId))));
    }
}
