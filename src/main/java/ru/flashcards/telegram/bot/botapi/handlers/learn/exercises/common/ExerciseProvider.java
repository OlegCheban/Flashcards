package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.utils.Lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;
import static ru.flashcards.telegram.bot.botapi.ExerciseKinds.*;

@Component
@AllArgsConstructor
public class ExerciseProvider {
    private LearningExercisesDao learningExercisesDao;
    private UserProfileFlashcardsDao userProfileFlashcardsDao;

    public BotApiMethod<?> newExercise (Long chatId){
        ExerciseFlashcard currentExercise = learningExercisesDao.findCurrentExerciseCard(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        List<String> wrongAnswers = null;

        if (currentExercise.exerciseKindsCode().equals(MEMORISED)) {
            sendMessage.setText("*" + currentExercise.word() + "* /" + currentExercise.transcription() + "/\n" + currentExercise.description() + "\n\n*Перевод:* " + currentExercise.translation());
            replyKeyboardMarkup.setKeyboard(memorisedKeyboard());

        } else if (currentExercise.exerciseKindsCode().equals(CHECK_DESCRIPTION)){
            wrongAnswers = learningExercisesDao.getRandomDescriptions();
            sendMessage.setText("Выберите подходящее описание для *" + currentExercise.word() + "* /" + currentExercise.transcription() + "/\n\n");
            replyKeyboardMarkup.setKeyboard(answersKeyboard(wrongAnswers, currentExercise.description()));

        } else if (currentExercise.exerciseKindsCode().equals(CHECK_TRANSLATION)){
            wrongAnswers = learningExercisesDao.getRandomTranslations();
            sendMessage.setText("Выберите правильный перевод для *" + currentExercise.word() + "* /" + currentExercise.transcription() + "/\n\n");
            replyKeyboardMarkup.setKeyboard(answersKeyboard(wrongAnswers, currentExercise.translation()));

        } else if (currentExercise.exerciseKindsCode().equals(CHECK_SPELLING) || currentExercise.exerciseKindsCode().equals(CHECK_SPELLING_WITH_HELPS)){
            List<KeyboardRow> keyboard = new ArrayList<>();
            keyboard.add(createButton(STOP_LEARNING.command));
            String messageText = String.format("Напишите на английском слово *%s*", currentExercise.translation());

            if (currentExercise.exerciseKindsCode().equals(CHECK_SPELLING_WITH_HELPS)) {
                messageText = messageText + "\n\nПодсказка: " + hideLetters(currentExercise.word()).replaceAll ("\\*","\\\\*");
            }
            sendMessage.setText(messageText);
            replyKeyboardMarkup.setKeyboard(keyboard);

        } else if (currentExercise.exerciseKindsCode().equals(COMPLETE_THE_GAPS)){
            wrongAnswers = userProfileFlashcardsDao.findUnlearnedFlashcardKeyword(chatId, 4);
            if (CollectionUtils.isNotEmpty(wrongAnswers) && wrongAnswers.size() != 4){
                wrongAnswers.remove(currentExercise.word());
            } else {
                wrongAnswers = learningExercisesDao.getRandomWords();
            }

            sendMessage.setText("Выберите корректное слово для заполнения пробела в предложении. \n\n" + currentExercise.example().replaceAll("\\*([a-zA-Z]+)\\*", "\\\\_\\\\_\\\\_\\\\_"));
            replyKeyboardMarkup.setKeyboard(answersKeyboard(wrongAnswers, currentExercise.word()));
        }

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    private String hideLetters(String word){
        StringBuilder result = new StringBuilder(word);
        double hidePrc = 80;
        int hiddenLettersQty = (int) (word.length()/100d*hidePrc);
        new Random().ints(1, word.length()).distinct().limit(hiddenLettersQty).forEach(
                i -> result.setCharAt(i, '*' )
        );

        return String.valueOf(result);
    }

    private List<KeyboardRow> memorisedKeyboard(){
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(createButton("Запомнил"));
        keyboard.add(createButton("Выйти"));

        return keyboard;
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
