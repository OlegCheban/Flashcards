package ru.flashcards.telegram.bot.botapi.handlers.preposition;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionLearningMode;

/**
 * Ends the preposition training early and shows the result.
 */
@Component
@RequiredArgsConstructor
public class StopPrepositionModeHandler implements MessageHandler {
    private final PrepositionLearningMode prepositionLearningMode;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        return prepositionLearningMode.stop(message.getChatId());
    }
}
