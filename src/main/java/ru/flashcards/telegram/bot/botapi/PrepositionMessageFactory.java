package ru.flashcards.telegram.bot.botapi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.handlers.preposition.CheckPrepositionMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.preposition.StopPrepositionModeHandler;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

@Component
@AllArgsConstructor
public class PrepositionMessageFactory implements MessageHandlerAbstractFactory<MessageHandler<Message>> {
    private StopPrepositionModeHandler stopPrepositionModeHandler;
    private CheckPrepositionMessageHandler checkPrepositionMessageHandler;

    @Override
    public MessageHandler<Message> getHandler(Message message) {
        if (message.getText().equals(STOP_LEARNING.command)){
            return stopPrepositionModeHandler;
        } else {
            return checkPrepositionMessageHandler;
        }
    }
}
