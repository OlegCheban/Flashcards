package ru.flashcards.telegram.bot.botapi.preposition;

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
import ru.flashcards.telegram.bot.botapi.training.TrainingSession;
import ru.flashcards.telegram.bot.botapi.training.TrainingSessionStore;

class PrepositionLearningModeTest {
    private static final long CHAT_ID = 1L;

    private final TrainingSessionStore<PrepositionExercise> sessionStore = new TrainingSessionStore<>();
    private final UserModeSettings userModeSettings = new UserModeSettings();
    private PrepositionLearningMode learningMode;

    @BeforeEach
    void setUp() throws IOException {
        Random random = new Random(42);
        learningMode = new PrepositionLearningMode(
                PrepositionCatalogTest.loadRealCatalog(),
                sessionStore,
                new PrepositionMessages(new AnswerKeyboardFactory(random), random),
                userModeSettings,
                random);
    }

    @Test
    void showsTopicsForUnknownTopic() {
        List<BotApiMethod<?>> replies = learningMode.start(CHAT_ID, "topics");

        assertEquals(1, replies.size());
        assertTrue(text(replies.get(0)).contains("/pr verbs"));
        assertNull(userModeSettings.getMode(CHAT_ID));
    }

    @Test
    void startsSessionOfTenQuestionsOnTopic() {
        List<BotApiMethod<?>> replies = learningMode.start(CHAT_ID, "Verbs");

        TrainingSession<PrepositionExercise> session = sessionStore.find(CHAT_ID).orElseThrow();
        assertEquals(UserMode.PREPOSITION, userModeSettings.getMode(CHAT_ID));
        assertEquals(PrepositionLearningMode.SESSION_SIZE, session.questionsQuantity());
        assertEquals(PrepositionCategory.VERB, session.currentQuestion().orElseThrow().category());
        assertTrue(text(replies.get(1)).startsWith("Вопрос 1/10"));
    }

    @Test
    void showsResultAndLeavesModeAfterLastQuestion() {
        learningMode.start(CHAT_ID, "");
        TrainingSession<PrepositionExercise> session = sessionStore.find(CHAT_ID).orElseThrow();

        List<BotApiMethod<?>> replies = List.of();
        for (int i = 0; i < PrepositionLearningMode.SESSION_SIZE; i++) {
            replies = learningMode.answer(CHAT_ID, session.currentQuestion().orElseThrow().mainAnswer());
        }

        assertTrue(text(replies.get(replies.size() - 1)).contains("Правильно с первой попытки: 10 из 10"));
        assertNull(userModeSettings.getMode(CHAT_ID));
        assertTrue(sessionStore.find(CHAT_ID).isEmpty());
    }

    @Test
    void explainsMistake() {
        learningMode.start(CHAT_ID, "adj");
        PrepositionExercise exercise = sessionStore.find(CHAT_ID).orElseThrow().currentQuestion().orElseThrow();

        List<BotApiMethod<?>> replies = learningMode.answer(CHAT_ID, exercise.distractors().get(0));

        String feedback = text(replies.get(0));
        assertTrue(feedback.contains("Правильный ответ: *" + exercise.mainAnswer() + "*"), feedback);
        assertTrue(feedback.contains(exercise.sentenceWithAnswer()), feedback);
    }

    @Test
    void stopsEarlyWithResult() {
        learningMode.start(CHAT_ID, "time");
        learningMode.answer(CHAT_ID, "nonsense");

        List<BotApiMethod<?>> replies = learningMode.stop(CHAT_ID);

        assertTrue(text(replies.get(0)).contains("0 из 1 (пройдено 1 из 10)"));
        assertNull(userModeSettings.getMode(CHAT_ID));
    }

    @Test
    void leavesModeWhenSessionIsLost() {
        userModeSettings.setMode(CHAT_ID, UserMode.PREPOSITION);

        List<BotApiMethod<?>> replies = learningMode.answer(CHAT_ID, "at");

        assertTrue(text(replies.get(0)).contains("Начните заново: /pr"));
        assertNull(userModeSettings.getMode(CHAT_ID));
    }

    private static String text(BotApiMethod<?> method) {
        return ((SendMessage) method).getText();
    }
}
