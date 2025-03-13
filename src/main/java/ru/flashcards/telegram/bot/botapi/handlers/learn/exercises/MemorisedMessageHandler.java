package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;

@Component
public class MemorisedMessageHandler extends CheckExerciseMessageHandler {
    public MemorisedMessageHandler(
            ExerciseProvider exerciseProvider,
            UserModeSettings userModeSettings,
            UserProfileFlashcardsDao userProfileFlashcardsDao,
            LearningExercisesDao learningExercisesDao) {
        super(exerciseProvider, userModeSettings, userProfileFlashcardsDao, learningExercisesDao);
    }

    @Override
    protected String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId) {
        return "Запомнил";
    }
}
