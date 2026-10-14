package ru.flashcards.telegram.bot.botapi.wateringSession;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.AnswerKeyboardFactory;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

/**
 * Asks to translate a learned flashcard: randomly from English or into English.
 */
@Component
@RequiredArgsConstructor
public class WateringSessionQuestion {
    private final FlashcardsDao flashcardsDao;
    private final WateringSessionTiming wateringSessionTiming;
    private final AnswerKeyboardFactory answerKeyboardFactory;
    private final Random random;

    public SendMessage newQuestion(Long chatId, UserFlashcard flashcard) {
        SendMessage message;
        if (random.nextBoolean()) {
            message = BotMessages.markdown(chatId, "*" + flashcard.word() + "*");
            message.setReplyMarkup(
                    answerKeyboardFactory.answers(flashcardsDao.getRandomTranslations(), flashcard.translation()));
        } else {
            message = BotMessages.markdown(chatId, "*" + flashcard.translation() + "*");
            message.setReplyMarkup(answerKeyboardFactory.answers(flashcardsDao.getRandomWords(), flashcard.word()));
        }

        wateringSessionTiming.start(chatId);
        return message;
    }
}
