package ru.flashcards.telegram.bot.botapi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.*;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;

import java.util.Collections;

@Component
@AllArgsConstructor
public class ExerciseMessageFactory implements MessageHandlerAbstractFactory<MessageHandler<Message>> {
    private MemorisedMessageHandler memorisedMessageHandler;
    private StopLearningMessageHandler stopLearningMessageHandler;
    private CheckDescriptionMessageHandler checkDescriptionMessageHandler;
    private CheckTranslationMessageHandler checkTranslationMessageHandler;
    private CheckSpellingMessageHandler checkSpellingMessageHandler;
    private CompleteTheGapsMessageHandler completeTheGapsMessageHandler;
    private LearningExercisesDao learningExercisesDao;

    @Override
    public MessageHandler<Message> getHandler(Message message) {
        if (message.getText().equals(BotCommand.STOP_LEARNING.command)){
            return stopLearningMessageHandler;
        } else {
            ExerciseFlashcard currentExercise = learningExercisesDao.findCurrentExerciseCard(message.getChatId());

            switch (currentExercise.exerciseKindsCode()){
                case MEMORISED:
                    return memorisedMessageHandler;
                case CHECK_DESCRIPTION:
                    return checkDescriptionMessageHandler;
                case CHECK_TRANSLATION:
                    return checkTranslationMessageHandler;
                case CHECK_SPELLING:
                case CHECK_SPELLING_WITH_HELPS:
                    return checkSpellingMessageHandler;
                case COMPLETE_THE_GAPS:
                    return completeTheGapsMessageHandler;
            }
        }

        return m -> Collections.emptyList();
    }
}
