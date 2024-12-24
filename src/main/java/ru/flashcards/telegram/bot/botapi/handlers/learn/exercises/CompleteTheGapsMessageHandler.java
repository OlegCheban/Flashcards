package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;

@Component
public class CompleteTheGapsMessageHandler extends CheckExerciseMessageHandler {
    private final LearningExercisesDao learningExercisesDao;

    public CompleteTheGapsMessageHandler(
            ExerciseProvider exerciseProvider,
            UserModeSettings userModeSettings,
            UserProfileFlashcardsDao userProfileFlashcardsDao,
            LearningExercisesDao learningExercisesDao) {
        super(exerciseProvider, userModeSettings, userProfileFlashcardsDao, learningExercisesDao);
        this.learningExercisesDao = learningExercisesDao;
    }

    @Override
    protected String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId) {
        ExerciseFlashcard currentExercise = learningExercisesDao.findCurrentExerciseCard(chatId);
        return currentExercise.word();
    }
}
