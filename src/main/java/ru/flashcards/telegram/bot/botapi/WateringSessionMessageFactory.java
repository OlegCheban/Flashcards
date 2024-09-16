package ru.flashcards.telegram.bot.botapi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.handlers.wateringSession.CheckWateringSessionExerciseMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.wateringSession.StopWateringSessionHandler;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

@Component
@AllArgsConstructor
public class WateringSessionMessageFactory implements MessageHandlerAbstractFactory<MessageHandler<Message>> {
    private StopWateringSessionHandler stopWateringSessionHandler;
    private CheckWateringSessionExerciseMessageHandler checkWateringSessionExerciseMessageHandler;

    @Override
    public MessageHandler<Message> getHandler(Message message) {
        if (message.getText().equals(STOP_LEARNING.command)){
            return stopWateringSessionHandler;
        } else {
            return checkWateringSessionExerciseMessageHandler;
        }
    }
}
