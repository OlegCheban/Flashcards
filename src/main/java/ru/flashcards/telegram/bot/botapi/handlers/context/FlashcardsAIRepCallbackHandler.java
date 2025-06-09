package ru.flashcards.telegram.bot.botapi.handlers.context;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;
import ru.flashcards.telegram.bot.services.FlashcardsContextService;
import ru.flashcards.telegram.bot.services.SendMessageService;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FlashcardsAIRepCallbackHandler implements MessageHandler<CallbackQuery> {
    private FlashcardsDao flashcardsDao;
    private FlashcardsContextService flashcardsContextService;
    private SendMessageService sendMessageService;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        Long flashcardId = callbackData.entityId();
        sendMessageService.sendMessage(message.getChatId(), "ИИ генерирует отчет ...");
        Flashcard flashcard = flashcardsDao.findFlashcardById(flashcardId);
        String text = flashcardsContextService.generateFlashcardReport(flashcard.word());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(text);
        sendMessage.enableMarkdown(true);

        list.add(sendMessage);

        return list;
    }
}