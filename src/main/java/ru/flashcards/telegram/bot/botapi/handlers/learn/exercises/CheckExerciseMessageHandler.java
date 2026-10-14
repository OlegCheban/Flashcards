package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.LearningSessionFinisher;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

/**
 * Checks the answer to the current exercise, saves the result and sends the next exercise.
 */
@Component
@RequiredArgsConstructor
public class CheckExerciseMessageHandler implements MessageHandler {
    private final LearningExercisesDao learningExercisesDao;
    private final ExerciseProvider exerciseProvider;
    private final LearningSessionFinisher learningSessionFinisher;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        Long chatId = message.getChatId();
        Optional<ExerciseFlashcard> currentExercise = learningExercisesDao.findCurrentExerciseCard(chatId);
        if (currentExercise.isEmpty()) {
            return List.of(learningSessionFinisher.finish(chatId));
        }

        ExerciseFlashcard exercise = currentExercise.get();
        boolean isCorrectAnswer = message.getText().trim().equalsIgnoreCase(expectedAnswer(exercise).trim());
        learningExercisesDao.insertExerciseResult(
                exercise.userFlashcardId(), exercise.exerciseKindsCode(), isCorrectAnswer);

        String verdict = isCorrectAnswer
                ? RandomMessageText.getPositiveMessage()
                : RandomMessageText.getNegativeMessage();
        return List.of(BotMessages.text(chatId, verdict), nextExercise(chatId));
    }

    private String expectedAnswer(ExerciseFlashcard exercise) {
        String answer = switch (exercise.exerciseKindsCode()) {
            case MEMORISED -> ExerciseProvider.MEMORISED_ANSWER;
            case CHECK_DESCRIPTION -> exercise.description();
            case CHECK_TRANSLATION -> exercise.translation();
            case CHECK_SPELLING, CHECK_SPELLING_WITH_HELPS, COMPLETE_THE_GAPS -> exercise.word();
        };
        return Objects.requireNonNullElse(answer, "");
    }

    private BotApiMethod<?> nextExercise(Long chatId) {
        Optional<ExerciseFlashcard> nextExercise = learningExercisesDao.findCurrentExerciseCard(chatId);
        if (nextExercise.isEmpty()) {
            return learningSessionFinisher.finish(chatId);
        }
        return exerciseProvider.newExercise(nextExercise.get());
    }
}
