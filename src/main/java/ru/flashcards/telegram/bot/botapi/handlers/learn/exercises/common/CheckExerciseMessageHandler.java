package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercises;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcards;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

import java.util.ArrayList;
import java.util.List;

public abstract class CheckExerciseMessageHandler implements MessageHandler<Message> {
    private ExerciseProvider exerciseProvider;
    private UserModeSettings userModeSettings;
    private UserProfileFlashcards userProfileFlashcards;
    private LearningExercises learningExercises;

    public CheckExerciseMessageHandler(
            ExerciseProvider exerciseProvider,
            UserModeSettings userModeSettings,
            UserProfileFlashcards userProfileFlashcards,
            LearningExercises learningExercises) {
        this.exerciseProvider = exerciseProvider;
        this.userModeSettings = userModeSettings;
        this.userProfileFlashcards = userProfileFlashcards;
        this.learningExercises = learningExercises;
    }

    protected abstract String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId);

    @Override
    public List<BotApiMethod<?>> handle(Message message){
        List<BotApiMethod<?>> list = new ArrayList<>();
        Long chatId = message.getChatId();

        ExerciseFlashcard currentExercise = learningExercises.findCurrentExerciseCard(chatId);
        list.add(createResultMessage(chatId, checkExercise(chatId, message.getText().trim(), currentExercise)));
        list.add(nextExercise(chatId));

        return list;
    }

    private BotApiMethod<?> createResultMessage(Long chatId, Boolean result){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(result ?
                RandomMessageText.getPositiveMessage() :
                RandomMessageText.getNegativeMessage()
        );

        return sendMessage;
    }

    private boolean checkExercise(Long chatId, String checkValue, ExerciseFlashcard currentExercise){
        Boolean isCorrentAnswer =
                checkValue.equalsIgnoreCase(
                    getCurrentExerciseFlashcardAttributeCheckValue(chatId).trim()
                );

        learningExercises.insertExerciseResult(
                currentExercise.userFlashcardId(),
                currentExercise.exerciseKindsCode(),
                isCorrentAnswer
        );

        return isCorrentAnswer;
    }

    private BotApiMethod<?> nextExercise(Long chatId){
        if (learningExercises.findCurrentExerciseCard(chatId) != null){
            return exerciseProvider.newExercise(chatId);
        } else {
            return stopLearning(chatId);
        }
    }

    private BotApiMethod<?> stopLearning(Long chatId){
        StringBuffer msg = new StringBuffer ();
        msg.append("Очень хорошо! Вы успешно выучили карточки:\n");
        userProfileFlashcards.findUserCardsForTraining(chatId).forEach(v -> {
            msg.append(v);
            msg.append("\n");
        });
        msg.append("\n");
        msg.append("Так держать!");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg.toString());
        sendMessage.setChatId(String.valueOf(chatId));

        //update learned flashcards
        userProfileFlashcards.refreshLearnedFlashcards();
        //disable learn mode
        userModeSettings.removeMode(chatId);

        //remove keyboard
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        return sendMessage;
    }
}
