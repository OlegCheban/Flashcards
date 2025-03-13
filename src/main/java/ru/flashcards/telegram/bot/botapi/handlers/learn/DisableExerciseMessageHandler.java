package ru.flashcards.telegram.bot.botapi.handlers.learn;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
@AllArgsConstructor
public class DisableExerciseMessageHandler implements MessageHandler<CallbackQuery> {
    private LearningExercisesDao learningExercisesDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        long messageId = message.getMessageId();
        long chatId = message.getChatId();

        learningExercisesDao.disableExercise(chatId, callbackData.entityCode());

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(String.valueOf(chatId));
        editMessage.setMessageId(toIntExact(messageId));
        editMessage.setText("Готово");

        list.add(editMessage);
        return list;
    }
}
