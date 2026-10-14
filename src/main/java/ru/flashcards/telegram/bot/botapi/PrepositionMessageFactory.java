package ru.flashcards.telegram.bot.botapi;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.handlers.preposition.CheckPrepositionMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.preposition.StopPrepositionModeHandler;

@Component
@RequiredArgsConstructor
public class PrepositionMessageFactory implements MessageHandlerAbstractFactory {
    private final StopPrepositionModeHandler stopPrepositionModeHandler;
    private final CheckPrepositionMessageHandler checkPrepositionMessageHandler;

    @Override
    public MessageHandler getHandler(Message message) {
        if (STOP_LEARNING.command().equals(message.getText())) {
            return stopPrepositionModeHandler;
        }
        return checkPrepositionMessageHandler;
    }
}
