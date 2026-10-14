package ru.flashcards.telegram.bot.botapi.training;

/**
 * A question of a {@link TrainingSession}.
 */
public interface TrainingQuestion {
    boolean isCorrect(String answer);
}
