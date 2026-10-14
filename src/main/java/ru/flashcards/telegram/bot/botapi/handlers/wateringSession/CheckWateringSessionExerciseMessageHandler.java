package ru.flashcards.telegram.bot.botapi.handlers.wateringSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionQuestion;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionTiming;
import ru.flashcards.telegram.bot.db.WateringSessionsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

/**
 * Checks the answer to the current watering session question and asks the next one.
 * Either the word or its translation is accepted, the answer must come within the user's reply time.
 */
@Component
@RequiredArgsConstructor
public class CheckWateringSessionExerciseMessageHandler implements MessageHandler {
    private final WateringSessionQuestion wateringSessionQuestion;
    private final WateringSessionTiming wateringSessionTiming;
    private final WateringSessionsDao wateringSessionsDao;
    private final StopWateringSessionHandler stopWateringSessionHandler;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        Long chatId = message.getChatId();
        Optional<UserFlashcard> flashcard = wateringSessionsDao.getUserFlashcardForWateringSession(chatId);
        if (flashcard.isEmpty()) {
            return stopWateringSessionHandler.handle(message);
        }

        List<BotApiMethod<?>> replies = new ArrayList<>();
        replies.add(BotMessages.text(chatId, resultText(chatId, message.getText().trim(), flashcard.get())));
        wateringSessionsDao.finishedLastFlashcard(flashcard.get().id());

        Optional<UserFlashcard> nextFlashcard = wateringSessionsDao.getUserFlashcardForWateringSession(chatId);
        if (nextFlashcard.isEmpty()) {
            replies.addAll(stopWateringSessionHandler.handle(message));
        } else {
            replies.add(wateringSessionQuestion.newQuestion(chatId, nextFlashcard.get()));
        }
        return replies;
    }

    private String resultText(Long chatId, String answer, UserFlashcard flashcard) {
        if (!wateringSessionTiming.isAnsweredInTime(chatId, wateringSessionsDao.getWateringSessionReplyTime(chatId))) {
            return "Time has run out.";
        }
        boolean isCorrectAnswer = answer.equalsIgnoreCase(flashcard.translation().trim())
                || answer.equalsIgnoreCase(flashcard.word().trim());
        return isCorrectAnswer ? RandomMessageText.getPositiveMessage() : RandomMessageText.getNegativeMessage();
    }
}
