package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;

@Component
public class MemorisedMessageHandler extends CheckExerciseMessageHandler {
    public MemorisedMessageHandler(DataLayerObject dataLayer, ExerciseProvider exerciseProvider, UserModeSettings userModeSettings) {
        super(dataLayer, exerciseProvider, userModeSettings);
    }

    @Override
    protected String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId) {
        return "Запомнил";
    }
}
