package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercises;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcards;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;

@Component
public class CheckSpellingMessageHandler extends CheckExerciseMessageHandler {
    private final LearningExercises learningExercises;

    public CheckSpellingMessageHandler(
            ExerciseProvider exerciseProvider,
            UserModeSettings userModeSettings,
            UserProfileFlashcards userProfileFlashcards,
            LearningExercises learningExercises) {
        super(exerciseProvider, userModeSettings, userProfileFlashcards, learningExercises);
        this.learningExercises = learningExercises;
    }

    @Override
    protected String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId) {
        ExerciseFlashcard currentExercise = learningExercises.findCurrentExerciseCard(chatId);
        return currentExercise.word();
    }
}
