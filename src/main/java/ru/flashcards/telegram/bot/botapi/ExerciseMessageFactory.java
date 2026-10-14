package ru.flashcards.telegram.bot.botapi;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.CheckExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.StopLearningMessageHandler;

@Component
@RequiredArgsConstructor
public class ExerciseMessageFactory implements MessageHandlerAbstractFactory {
    private final StopLearningMessageHandler stopLearningMessageHandler;
    private final CheckExerciseMessageHandler checkExerciseMessageHandler;

    @Override
    public MessageHandler getHandler(Message message) {
        if (STOP_LEARNING.command().equals(message.getText())) {
            return stopLearningMessageHandler;
        }
        return checkExerciseMessageHandler;
    }
}
