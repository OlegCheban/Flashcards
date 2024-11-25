package ru.flashcards.telegram.bot.botapi.handlers.examples;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FlashcardUsageExamplesCallbackHandler implements MessageHandler<CallbackQuery> {
    private DataLayerObject dataLayer;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        Long userFlashcardId = callbackData.entityId();

        dataLayer.getExamplesByUserFlashcardId(userFlashcardId).forEach(example -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText(example);
            sendMessage.enableMarkdown(true);

            list.add(sendMessage);
        });

        if (list.isEmpty()){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Для данной карточки нет примеров.");

            list.add(sendMessage);
        }

        return list;
    }
}
