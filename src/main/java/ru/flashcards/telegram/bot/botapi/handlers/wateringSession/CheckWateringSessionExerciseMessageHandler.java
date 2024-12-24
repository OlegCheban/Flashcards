package ru.flashcards.telegram.bot.botapi.handlers.wateringSession;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionQuestion;
import ru.flashcards.telegram.bot.db.dmlOps.WateringSessionsDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.UserFlashcard;
import ru.flashcards.telegram.bot.utils.RandomMessageText;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionTiming;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class CheckWateringSessionExerciseMessageHandler implements MessageHandler<Message> {
    private WateringSessionQuestion wateringSessionQuestion;
    private WateringSessionTiming wateringSessionTiming;
    private WateringSessionsDao wateringSessionsDao;
    private UserFlashcard userFlashcard;
    private List<BotApiMethod<?>> list;
    private Long chatId;

    public CheckWateringSessionExerciseMessageHandler(WateringSessionQuestion wateringSessionQuestion,
                                                      WateringSessionTiming wateringSessionTiming,
                                                      WateringSessionsDao wateringSessionsDao) {
        this.wateringSessionQuestion = wateringSessionQuestion;
        this.wateringSessionTiming = wateringSessionTiming;
        this.wateringSessionsDao = wateringSessionsDao;
    }

    @Override
    public List<BotApiMethod<?>> handle(Message message){
        list = new ArrayList<>();
        chatId = message.getChatId();
        userFlashcard = wateringSessionsDao.getUserFlashcardForWateringSession(chatId);

        createResultMessage(
                checkExercise(message.getText().trim()),
                checkTiming(chatId)
        );
        next();

        return list;
    }

    private void createResultMessage(Boolean checkExerciseResult, Boolean checkTimingResult){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (!checkTimingResult){
            sendMessage.setText("Time has run out.");
        } else {
            sendMessage.setText(checkExerciseResult ?
                    RandomMessageText.getPositiveMessage() :
                    RandomMessageText.getNegativeMessage()
            );
        }

        list.add(sendMessage);
    }

    private boolean checkExercise(String checkValue){
        return checkValue.equalsIgnoreCase(userFlashcard.translation().trim()) || checkValue.equalsIgnoreCase(userFlashcard.word().trim());
    }

    private Boolean checkTiming(Long chatId){
        LocalDateTime startExerciseDateTime = wateringSessionTiming.getStartDateTime(chatId);
        long diff = ChronoUnit.SECONDS.between(startExerciseDateTime, LocalDateTime.now());
        return diff <= wateringSessionsDao.getWateringSessionReplyTime(chatId);
    }

    private void next(){
        wateringSessionsDao.finishedLastFlashcard(userFlashcard.id());
        list.add(wateringSessionQuestion.newQuestion(chatId));
    }
}
