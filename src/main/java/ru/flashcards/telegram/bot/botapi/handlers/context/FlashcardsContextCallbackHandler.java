package ru.flashcards.telegram.bot.botapi.handlers.context;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.services.FlashcardsContextService;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FlashcardsContextCallbackHandler implements MessageHandler<CallbackQuery> {
    private LearningExercisesDao learningExercisesDao;
    private FlashcardsContextService flashcardsContextService;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();

        StringBuffer placeholders = new StringBuffer();
        learningExercisesDao.getRecentLearned(message.getChatId(), callbackData.entityId(), false)
                .forEach(v -> placeholders.append(v+", "));

        String text = flashcardsContextService.generateContext(placeholders.toString());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(text);
        sendMessage.enableMarkdown(true);

        list.add(sendMessage);

        return list;
    }
}