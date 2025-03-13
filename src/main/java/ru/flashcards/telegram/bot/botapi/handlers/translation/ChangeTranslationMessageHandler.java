package ru.flashcards.telegram.bot.botapi.handlers.translation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserMessageTypeBuffer;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ChangeTranslationMessageHandler implements MessageHandler<Message> {
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    private UserMessageTypeBuffer userMessageTypeBuffer;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        List<BotApiMethod<?>> list = new ArrayList<>();

        userProfileFlashcardsDao.editTranslation(
                userMessageTypeBuffer.getEntityId(message.getChatId()),
                message.getText()
        );
        userMessageTypeBuffer.removeRequest(message.getChatId());

        SendMessage replyMessage = new SendMessage();
        replyMessage.setText("Translation is changed successfully");
        replyMessage.setChatId(String.valueOf(message.getChatId()));
        list.add(replyMessage);

        return list;
    }
}
