package ru.flashcards.telegram.bot.botapi;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.handlers.phrasalverb.CheckPhrasalVerbMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.phrasalverb.StopPhrasalVerbModeHandler;

@Component
@RequiredArgsConstructor
public class PhrasalVerbMessageFactory implements MessageHandlerAbstractFactory {
    private final StopPhrasalVerbModeHandler stopPhrasalVerbModeHandler;
    private final CheckPhrasalVerbMessageHandler checkPhrasalVerbMessageHandler;

    @Override
    public MessageHandler getHandler(Message message) {
        if (STOP_LEARNING.command().equals(message.getText())) {
            return stopPhrasalVerbModeHandler;
        }
        return checkPhrasalVerbMessageHandler;
    }
}
