package ru.flashcards.telegram.bot.botapi.handlers.learn;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.dto.Flashcard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
@AllArgsConstructor
public class ExcludeCallbackHandler implements MessageHandler<CallbackQuery> {
    private DataLayerObject dataLayer;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        long messageId = message.getMessageId();
        long chatId = message.getChatId();
        Long flashcardId = callbackData.entityId();

        Flashcard flashcard = dataLayer.findFlashcardById(flashcardId);
        dataLayer.exceptFlashcard(chatId, flashcardId);

        EditMessageText translationMessage = new EditMessageText();
        translationMessage.setChatId(String.valueOf(chatId));
        translationMessage.setMessageId(toIntExact(messageId));
        translationMessage.enableMarkdown(true);
        translationMessage.setText("Карточка *" + flashcard.word() + "* успешно исключена. Бот больше не будет Вам ее предлагать");

        list.add(translationMessage);
        return list;
    }
}
