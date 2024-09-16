package ru.flashcards.telegram.bot.botapi.preposition;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.flashcards.telegram.bot.utils.Lambda;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

@Component
@AllArgsConstructor
public class PrepositionLearningMode {
    private Preposition preposition;
    private UserPrepositionSettings userPrepositionSettings;

    public BotApiMethod<?> newPreposition (Long chatId){
        var id = userPrepositionSettings.getLatestPrepositionId(chatId);
        if (id != null){
            var pr = preposition.getPrepositions().get(id);
            var text = pr.get(0);
            var correctAnswer = pr.get(1);
            var secondCorrectAnswer = pr.size() == 3 ? pr.get(2) : "";
            return sentExercise(chatId, text, correctAnswer, secondCorrectAnswer);
        } else {
            for (Map.Entry<Integer, List<String>> entry : preposition.getPrepositions().entrySet()) {
                var text = entry.getValue().get(0);
                var correctAnswer = entry.getValue().get(1);
                var secondCorrectAnswer = entry.getValue().size() == 3 ? entry.getValue().get(2) : "";
                userPrepositionSettings.setCurrentPrepositionId(chatId, 1);
                return sentExercise(chatId, text, correctAnswer, secondCorrectAnswer);
            }
        }

        return sendSimpleMessage(chatId);
    }

    private BotApiMethod<?> sentExercise(Long chatId, String text, String correctAnswer, String secondCorrectAnswer){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        sendMessage.setText(text);
        replyKeyboardMarkup.setKeyboard(answersKeyboard(getWrongAnswers(correctAnswer, secondCorrectAnswer), correctAnswer));
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    private List<String> getWrongAnswers(String correctAnswer, String secondCorrectAnswer){
        List<String> wrongAnswers = new ArrayList<>();
        wrongAnswers.add("about");
        wrongAnswers.add("with");
        wrongAnswers.add("for");
        wrongAnswers.add("to");
        wrongAnswers.add("of");
        wrongAnswers.add("by");
        wrongAnswers.add("at");

        wrongAnswers.remove(correctAnswer);
        wrongAnswers.remove(secondCorrectAnswer);

        Collections.shuffle(wrongAnswers);
        return wrongAnswers.subList(0, 3);
    }

    private BotApiMethod<?> sendSimpleMessage(Long chatId){
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Нет данных");
        return message;
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
