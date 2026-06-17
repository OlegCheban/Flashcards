package ru.flashcards.telegram.bot.botapi.handlers.preposition;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.preposition.UserPrepositionMistakes;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class StopPrepositionModeHandler implements MessageHandler<Message> {
    private UserModeSettings userModeSettings;
    private UserPrepositionMistakes userPrepositionMistakes;
    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        List<BotApiMethod<?>> list = new ArrayList<>();
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        userModeSettings.removeMode(message.getChatId());

        StringBuilder msg = new StringBuilder();


        var mistakes = userPrepositionMistakes.getMistakes(message.getChatId());
        if (!mistakes.isEmpty()){
            for (var entry : mistakes.entrySet()) {
                msg.append(entry.getKey());
                msg.append("\n");
                msg.append(String.format("Кол-во ошибок: %s", entry.getValue()));
                msg.append("\n");
                msg.append("\n");
            }
            userPrepositionMistakes.clearMistakes(message.getChatId());
        }
        msg.append("\n");
        msg.append("Так держать!");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        sendMessage.setText(msg.toString());

        list.add(sendMessage);

        return list;
    }
}
