package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;

@Component
public class CheckSpellingMessageHandler extends CheckExerciseMessageHandler {
    private final LearningExercisesDao learningExercisesDao;

    public CheckSpellingMessageHandler(
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
