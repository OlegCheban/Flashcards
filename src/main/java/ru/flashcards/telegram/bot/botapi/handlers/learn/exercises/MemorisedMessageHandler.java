package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercises;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcards;

@Component
public class MemorisedMessageHandler extends CheckExerciseMessageHandler {
    public MemorisedMessageHandler(
            ExerciseProvider exerciseProvider,
            UserModeSettings userModeSettings,
            UserProfileFlashcards userProfileFlashcards,
            LearningExercises learningExercises) {
        super(exerciseProvider, userModeSettings, userProfileFlashcards, learningExercises);
    }

    @Override
    protected String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId) {
        return "Запомнил";
    }
}
