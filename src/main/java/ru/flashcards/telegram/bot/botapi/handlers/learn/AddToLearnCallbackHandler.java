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
public class AddToLearnCallbackHandler implements MessageHandler<CallbackQuery> {
    private DataLayerObject dataLayer;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        Message message = callbackQuery.getMessage();
        long messageId = message.getMessageId();
        long chatId = message.getChatId();
        Long flashcardId = callbackData.entityId();
        Flashcard flashcard = dataLayer.findFlashcardById(flashcardId);
        EditMessageText resultMessage = new EditMessageText();
        resultMessage.setChatId(String.valueOf(chatId));
        resultMessage.setMessageId(toIntExact(messageId));
        resultMessage.enableMarkdown(true);

        if (dataLayer.findUserFlashcardByName(chatId, flashcard.word()) == null){
            dataLayer.addUserFlashcard(flashcard.word(), flashcard.description(), flashcard.transcription(),
                    flashcard.translation(), flashcard.categoryId(), chatId);
            resultMessage.setText("Карточка *" + flashcard.word() + "* добавлена для изучения");
        } else {
            resultMessage.setText("Карточка *" + flashcard.word() + "* была ранее добавлена для изучения");
        }

        list.add(resultMessage);
        return list;
    }
}