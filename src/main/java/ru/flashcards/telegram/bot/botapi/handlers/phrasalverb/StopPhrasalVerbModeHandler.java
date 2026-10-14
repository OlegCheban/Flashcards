package ru.flashcards.telegram.bot.botapi.handlers.phrasalverb;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.phrasalverb.PhrasalVerbTrainingMode;

/**
 * Ends the phrasal verb training early and shows the result.
 */
@Component
@RequiredArgsConstructor
public class StopPhrasalVerbModeHandler implements MessageHandler {
    private final PhrasalVerbTrainingMode phrasalVerbTrainingMode;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        return phrasalVerbTrainingMode.stop(message.getChatId());
    }
}
