package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;

@Component
public class CheckSpellingMessageHandler extends CheckExerciseMessageHandler {
    private DataLayerObject dataLayer;
    public CheckSpellingMessageHandler(DataLayerObject dataLayer, ExerciseProvider exerciseProvider, UserModeSettings userModeSettings) {
        super(dataLayer, exerciseProvider, userModeSettings);
        this.dataLayer = dataLayer;
    }

    @Override
    protected String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId) {
        ExerciseFlashcard currentExercise = dataLayer.getCurrentExercise(chatId);
        return currentExercise.word();
    }
}
