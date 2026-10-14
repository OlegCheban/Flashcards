package ru.flashcards.telegram.bot.botapi.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TrainingSessionTest {
    private final Question first = new Question("first");
    private final Question second = new Question("second");
    private final Question third = new Question("third");
    private final Question fourth = new Question("fourth");

    @Test
    void countsCorrectAnswersAndFinishes() {
        TrainingSession<Question> session = new TrainingSession<>(List.of(first, second));

        assertTrue(session.answer("ok").orElseThrow().isCorrect());
        assertTrue(session.answer("ok").orElseThrow().isCorrect());

        assertTrue(session.isFinished());
        assertEquals(new TrainingSession.Summary<>(2, 2, 2, List.of()), session.summary());
    }

    @Test
    void asksWrongAnswerAgainAfterTwoOtherQuestions() {
        TrainingSession<Question> session = new TrainingSession<>(List.of(first, second, third, fourth));

        assertFalse(session.answer("wrong").orElseThrow().isCorrect());

        assertEquals(List.of(second, third, first, fourth), remainingOrder(session));
    }

    @Test
    void marksRepeatedQuestionAndKeepsQuestionNumber() {
        TrainingSession<Question> session = new TrainingSession<>(List.of(first, second, third));
        session.answer("wrong");
        session.answer("ok");
        session.answer("ok");

        assertTrue(session.isCurrentRepeated());
        assertEquals(first, session.currentQuestion().orElseThrow());
        assertEquals(3, session.currentQuestionNumber());
        assertEquals("Повтор", TrainingTexts.questionHeader(session));
    }

    @Test
    void scoresFirstAttemptOnlyAndRemembersMistakes() {
        TrainingSession<Question> session = new TrainingSession<>(List.of(first));
        session.answer("wrong");
        session.answer("ok");

        assertTrue(session.isFinished());
        assertEquals(new TrainingSession.Summary<>(0, 1, 1, List.of(first)), session.summary());
    }

    @Test
    void repeatsQuestionAtMostTwice() {
        TrainingSession<Question> session = new TrainingSession<>(List.of(first));

        int asked = 0;
        while (!session.isFinished()) {
            session.answer("wrong");
            asked++;
        }

        assertEquals(1 + TrainingSession.MAX_REPEATS, asked);
    }

    @Test
    void ignoresAnswerAfterFinish() {
        TrainingSession<Question> session = new TrainingSession<>(List.of(first));
        session.answer("ok");

        assertTrue(session.answer("ok").isEmpty());
    }

    private List<Question> remainingOrder(TrainingSession<Question> session) {
        List<Question> order = new ArrayList<>();
        while (!session.isFinished()) {
            order.add(session.currentQuestion().orElseThrow());
            session.answer("ok");
        }
        return order;
    }

    private record Question(String name) implements TrainingQuestion {
        @Override
        public boolean isCorrect(String answer) {
            return "ok".equals(answer);
        }
    }
}
