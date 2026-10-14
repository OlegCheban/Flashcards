package ru.flashcards.telegram.bot.botapi.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A training session: a fixed set of questions asked one by one.
 * A question answered wrongly is asked again after {@value #REPEAT_DISTANCE} other questions,
 * at most {@value #MAX_REPEATS} times. The score counts first attempts only.
 * The methods are synchronized because a user can send the next answer before the previous one is processed.
 */
public class TrainingSession<Q extends TrainingQuestion> {
    static final int REPEAT_DISTANCE = 2;
    static final int MAX_REPEATS = 2;

    private final int questionsQuantity;
    private final List<Q> queue;
    private final Set<Q> attempted = new HashSet<>();
    private final Map<Q, Integer> repeats = new HashMap<>();
    private final Set<Q> mistakes = new LinkedHashSet<>();
    private int correctFirstAttempts;

    public TrainingSession(List<Q> questions) {
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("A training session needs at least one question");
        }
        this.questionsQuantity = questions.size();
        this.queue = new ArrayList<>(questions);
    }

    public synchronized Optional<Q> currentQuestion() {
        return queue.isEmpty() ? Optional.empty() : Optional.of(queue.get(0));
    }

    public synchronized boolean isCurrentRepeated() {
        return !queue.isEmpty() && attempted.contains(queue.get(0));
    }

    /**
     * @return the number of the current question among the {@link #questionsQuantity()} new ones
     */
    public synchronized int currentQuestionNumber() {
        return isCurrentRepeated() ? attempted.size() : attempted.size() + 1;
    }

    public int questionsQuantity() {
        return questionsQuantity;
    }

    /**
     * Checks the answer to the current question and moves to the next one.
     *
     * @return empty if the session is already finished
     */
    public synchronized Optional<AnswerResult<Q>> answer(String answer) {
        if (queue.isEmpty()) {
            return Optional.empty();
        }

        Q question = queue.remove(0);
        boolean isFirstAttempt = attempted.add(question);
        boolean isCorrect = question.isCorrect(answer);
        if (isCorrect && isFirstAttempt) {
            correctFirstAttempts++;
        }
        if (!isCorrect) {
            mistakes.add(question);
            repeatLater(question);
        }
        return Optional.of(new AnswerResult<>(question, isCorrect));
    }

    public synchronized boolean isFinished() {
        return queue.isEmpty();
    }

    public synchronized Summary<Q> summary() {
        return new Summary<>(correctFirstAttempts, attempted.size(), questionsQuantity, List.copyOf(mistakes));
    }

    private void repeatLater(Q question) {
        int repeated = repeats.getOrDefault(question, 0);
        if (repeated >= MAX_REPEATS) {
            return;
        }
        repeats.put(question, repeated + 1);
        queue.add(Math.min(REPEAT_DISTANCE, queue.size()), question);
    }

    public record AnswerResult<Q>(Q question, boolean isCorrect) {
    }

    /**
     * @param correctFirstAttempts questions answered correctly at the first attempt
     * @param answeredQuestions    questions asked at least once
     * @param mistakes             questions answered wrongly at least once, in the order of the first mistake
     */
    public record Summary<Q>(
            int correctFirstAttempts,
            int answeredQuestions,
            int questionsQuantity,
            List<Q> mistakes) {
    }
}
