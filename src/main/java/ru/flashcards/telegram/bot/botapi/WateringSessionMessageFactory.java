package ru.flashcards.telegram.bot.botapi;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.handlers.wateringSession.CheckWateringSessionExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.wateringSession.StopWateringSessionHandler;

@Component
@RequiredArgsConstructor
public class WateringSessionMessageFactory implements MessageHandlerAbstractFactory {
    private final StopWateringSessionHandler stopWateringSessionHandler;
    private final CheckWateringSessionExerciseMessageHandler checkWateringSessionExerciseMessageHandler;

    @Override
    public MessageHandler getHandler(Message message) {
        if (STOP_LEARNING.command().equals(message.getText())) {
            return stopWateringSessionHandler;
        }
        return checkWateringSessionExerciseMessageHandler;
    }
}
