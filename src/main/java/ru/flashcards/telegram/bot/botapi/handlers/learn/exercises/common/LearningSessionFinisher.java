package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;

/**
 * Ends a learning session: marks the learned flashcards and leaves the exercise mode.
 */
@Component
@RequiredArgsConstructor
public class LearningSessionFinisher {
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final UserModeSettings userModeSettings;

    public SendMessage finish(Long chatId) {
        StringBuilder text = new StringBuilder();
        List<String> learned = userProfileFlashcardsDao.findLearnedFlashcards(chatId);
        if (!learned.isEmpty()) {
            text.append("Очень хорошо! Вы успешно выучили карточки:\n")
                    .append(String.join("\n", learned))
                    .append("\n\n");
        }
        text.append("Так держать!");

        userProfileFlashcardsDao.refreshLearnedFlashcards();
        userModeSettings.removeMode(chatId);

        return BotMessages.withoutKeyboard(BotMessages.text(chatId, text.toString()));
    }
}
