package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.LearningSessionFinisher;

@Component
@RequiredArgsConstructor
public class StopLearningMessageHandler implements MessageHandler {
    private final LearningSessionFinisher learningSessionFinisher;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        return List.of(learningSessionFinisher.finish(message.getChatId()));
    }
}
