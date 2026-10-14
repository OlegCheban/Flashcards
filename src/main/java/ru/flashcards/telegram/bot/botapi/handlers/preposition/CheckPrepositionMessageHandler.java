package ru.flashcards.telegram.bot.botapi.handlers.preposition;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionLearningMode;

/**
 * An answer to the current preposition exercise.
 */
@Component
@RequiredArgsConstructor
public class CheckPrepositionMessageHandler implements MessageHandler {
    private final PrepositionLearningMode prepositionLearningMode;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        return prepositionLearningMode.answer(message.getChatId(), message.getText());
    }
}
