package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class StopLearningMessageHandler implements MessageHandler<Message> {
    private DataLayerObject dataLayer;
    private UserModeSettings userModeSettings;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        List<BotApiMethod<?>> list = new ArrayList<>();
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);

        StringBuffer msg = new StringBuffer ();
        List<String> learned = dataLayer.getLearnedFlashcards(message.getChatId());
        if (!learned.isEmpty()){
            msg.append("Очень хорошо! Вы успешно выучили карточки:\n");
            learned.forEach(v -> {
                msg.append(v);
                msg.append("\n");
            });
            msg.append("\n");
        }

        msg.append("Так держать!");

        //update learned flashcards
        dataLayer.refreshLearnedFlashcards();
        //disable learn mode
        userModeSettings.removeMode(message.getChatId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText(msg.toString());
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        list.add(sendMessage);

        return list;
    }
}
