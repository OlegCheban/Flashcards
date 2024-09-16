package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

import java.util.ArrayList;
import java.util.List;

public abstract class CheckExerciseMessageHandler implements MessageHandler<Message> {
    private DataLayerObject dataLayer;
    private ExerciseProvider exerciseProvider;
    private UserModeSettings userModeSettings;

    public CheckExerciseMessageHandler(DataLayerObject dataLayer, ExerciseProvider exerciseProvider, UserModeSettings userModeSettings) {
        this.dataLayer = dataLayer;
        this.exerciseProvider = exerciseProvider;
        this.userModeSettings = userModeSettings;
    }

    protected abstract String getCurrentExerciseFlashcardAttributeCheckValue(Long chatId);

    @Override
    public List<BotApiMethod<?>> handle(Message message){
        List<BotApiMethod<?>> list = new ArrayList<>();
        Long chatId = message.getChatId();

        ExerciseFlashcard currentExercise = dataLayer.getCurrentExercise(chatId);
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

        dataLayer.insertExerciseResult(
                currentExercise.userFlashcardId(),
                currentExercise.exerciseKindsCode(),
                isCorrentAnswer
        );

        return isCorrentAnswer;
    }

    private BotApiMethod<?> nextExercise(Long chatId){
        if (dataLayer.getCurrentExercise(chatId) != null){
            return exerciseProvider.newExercise(chatId);
        } else {
            return stopLearning(chatId);
        }
    }

    private BotApiMethod<?> stopLearning(Long chatId){
        StringBuffer msg = new StringBuffer ();
        msg.append("Очень хорошо! Вы успешно выучили карточки:\n");
        dataLayer.getCurrentBatchFlashcardsByUser(chatId).forEach(v -> {
            msg.append(v);
            msg.append("\n");
        });
        msg.append("\n");
        msg.append("Так держать!");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg.toString());
        sendMessage.setChatId(String.valueOf(chatId));

        //update learned flashcards
        dataLayer.refreshLearnedFlashcards();
        //disable learn mode
        userModeSettings.removeMode(chatId);

        //remove keyboard
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        return sendMessage;
    }
}
