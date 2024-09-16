package ru.flashcards.telegram.bot.botapi.wateringSession;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.dto.UserFlashcard;
import ru.flashcards.telegram.bot.utils.Lambda;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

@Component
@AllArgsConstructor
public class WateringSession {
    private DataLayerObject dataLayer;
    private WateringSessionTiming wateringSessionTiming;

    public BotApiMethod<?> newFlashcard (Long chatId){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 2);
        UserFlashcard userFlashcard = dataLayer.getUserFlashcardForWateringSession(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<String> wrongAnswers = null;

        if (randomNum == 0){
            wrongAnswers = dataLayer.getRandomTranslations();
            sendMessage.setText("*"+userFlashcard.word()+"*");
            replyKeyboardMarkup.setKeyboard(answersKeyboard(wrongAnswers, userFlashcard.translation()));
        } else {
            wrongAnswers = dataLayer.getRandomWords();
            sendMessage.setText("*"+userFlashcard.translation()+"*");
            replyKeyboardMarkup.setKeyboard(answersKeyboard(wrongAnswers, userFlashcard.word()));
        }

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        wateringSessionTiming.setStartDateTime(chatId, LocalDateTime.now());
        return sendMessage;
    }

    private List<KeyboardRow> answersKeyboard(List<String> wrongAnswersList, String correctAnswer){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
        final int lastButton = 3;
        List<KeyboardRow> keyboard = new ArrayList<>();

        wrongAnswersList.forEach(Lambda.forEachWithCounter( (i, v) -> {
            if (i == randomNum){
                keyboard.add(createButton(correctAnswer));
            }
            keyboard.add(createButton(v));

            if (i == wrongAnswersList.size() && randomNum == lastButton){
                keyboard.add(createButton(correctAnswer));
            }
        }));
        keyboard.add(createButton(STOP_LEARNING.command));

        return keyboard;
    }

    private KeyboardRow createButton(String text){
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(text));

        return row;
    }
}
