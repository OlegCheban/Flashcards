package ru.flashcards.telegram.bot.botapi.phrasalverb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import ru.flashcards.telegram.bot.botapi.UserMode;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.keyboard.AnswerKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.training.TrainingSessionStore;

class PhrasalVerbTrainingModeTest {
    private static final long CHAT_ID = 1L;

    private final TrainingSessionStore<PhrasalVerbQuestion> sessionStore = new TrainingSessionStore<>();
    private final UserModeSettings userModeSettings = new UserModeSettings();
    private PhrasalVerbTrainingMode trainingMode;

    @BeforeEach
    void setUp() throws IOException {
        Random random = new Random(3);
        trainingMode = new PhrasalVerbTrainingMode(
                new PhrasalVerbQuestionFactory(PhrasalVerbCatalogTest.loadRealCatalog(), random),
                sessionStore,
                new PhrasalVerbMessages(new AnswerKeyboardFactory(random)),
                userModeSettings);
    }

    @Test
    void startsSessionWithNumberedOptions() {
        List<BotApiMethod<?>> replies = trainingMode.start(CHAT_ID);

        PhrasalVerbQuestion question = currentQuestion();
        String text = text(replies.get(1));
        assertEquals(UserMode.PHRASAL_VERB, userModeSettings.getMode(CHAT_ID));
        assertTrue(text.startsWith("Вопрос 1/10"), text);
        assertTrue(text.contains("Выберите подходящее значение для *" + question.verb().verb() + "*"), text);
        assertTrue(text.contains("\n4. "), text);
    }

    @Test
    void showsResultAndLeavesModeAfterLastQuestion() {
        trainingMode.start(CHAT_ID);

        List<BotApiMethod<?>> replies = List.of();
        for (int i = 0; i < PhrasalVerbTrainingMode.SESSION_SIZE; i++) {
            replies = trainingMode.answer(CHAT_ID, String.valueOf(currentQuestion().correctOptionNumber()));
        }

        assertTrue(text(replies.get(replies.size() - 1)).contains("Правильно с первой попытки: 10 из 10"));
        assertNull(userModeSettings.getMode(CHAT_ID));
        assertTrue(sessionStore.find(CHAT_ID).isEmpty());
    }

    @Test
    void showsCorrectOptionAndExampleAfterMistake() {
        trainingMode.start(CHAT_ID);
        PhrasalVerbQuestion question = currentQuestion();
        int wrongNumber = question.correctOptionNumber() % question.options().size() + 1;

        List<BotApiMethod<?>> replies = trainingMode.answer(CHAT_ID, String.valueOf(wrongNumber));

        String feedback = text(replies.get(0));
        assertTrue(feedback.contains("Правильный ответ: " + question.correctOptionNumber() + "."), feedback);
        assertTrue(feedback.contains("Пример: "), feedback);
    }

    @Test
    void stopsEarlyWithResult() {
        trainingMode.start(CHAT_ID);
        trainingMode.answer(CHAT_ID, "nonsense");

        List<BotApiMethod<?>> replies = trainingMode.stop(CHAT_ID);

        assertTrue(text(replies.get(0)).contains("0 из 1 (пройдено 1 из 10)"));
        assertNull(userModeSettings.getMode(CHAT_ID));
    }

    private PhrasalVerbQuestion currentQuestion() {
        return sessionStore.find(CHAT_ID).orElseThrow().currentQuestion().orElseThrow();
    }

    private static String text(BotApiMethod<?> method) {
        return ((SendMessage) method).getText();
    }
}
