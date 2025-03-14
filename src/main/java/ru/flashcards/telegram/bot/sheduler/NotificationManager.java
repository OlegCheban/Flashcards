package ru.flashcards.telegram.bot.sheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardSpacedRepetitionNotification;
import ru.flashcards.telegram.bot.service.SendService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.*;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.RTL;

@Component
public class NotificationManager {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String pushpinEmoji = "\uD83D\uDCCC";
    private final String alarmEmoji = "\u23F0";

    @Autowired
    private SendService sendService;
    @Autowired
    private NotificationsDao notificationsDao;

    @Scheduled(cron = "0 * * * * *")
    private void run(){
        randomNotification();
        spacedRepetitionNotification();
    }

    private void randomNotification() {
        List<UserFlashcardPushMono> userFlashcardPushMonos = notificationsDao.getUserFlashcardsRandomNotification();
        userFlashcardPushMonos.forEach((queue) -> {
            List<JSONObject> listButtons = new ArrayList<>();
            listButtons.add(prepareButton(queue.userFlashcardId(), "Перевод", TRANSLATE));
            listButtons.add(prepareButton(queue.userFlashcardId(), "Примеры", EXS));
            sendService.sendMessage(
                    queue.userId(),
                    "*"+queue.word()+"* /" + queue.transcription() + "/ " + pushpinEmoji + "\n\n"+queue.description(),
                    String.valueOf(createButtonMenu(listButtons))
            );
            notificationsDao.updatePushTimestamp(queue.userFlashcardId());
        });
    }

    private void spacedRepetitionNotification(){
        if (!notificationsDao.isPushQueueUpToCurrentDate()){
            notificationsDao.refreshIntervalNotification();
        }
        List<UserFlashcardSpacedRepetitionNotification> userFlashcardSpacedRepetitionNotifications =
                notificationsDao.getUserFlashcardsSpacedRepetitionNotification();

        userFlashcardSpacedRepetitionNotifications.forEach((queue) -> {
            if (queue.notificationDate().isBefore(LocalDateTime.now())){
                List<JSONObject> listButtons = new ArrayList<>();
                listButtons.add(prepareButton(queue.userFlashcardId(), "Да", PROCEED));
                listButtons.add(prepareButton(queue.userFlashcardId(), "Нет", RTL));

                sendService.sendMessage(queue.userId(),
                        "*Интервальное повторение* " + alarmEmoji +
                                "\n*" + queue.word()+ "* /" + queue.transcription() + "/ ("+queue.prc()+"% выучено)" +
                                "\n\n Попытайтесь вспомнить выученное слово. Нажмите да если помните",
                        String.valueOf(createButtonMenu(listButtons)));
                notificationsDao.addFlashcardPushHistory(queue.userFlashcardId());
            }
        });
    }

    private JSONObject prepareButton(Long userFlashcardId, String label, BotKeyboardButton command){
        CallbackData callbackData = new CallbackData(command, userFlashcardId, new SwiperParams("", ""));
        JSONObject inlineKeyboardButtonJson = new JSONObject();
        inlineKeyboardButtonJson.put("text", label);

        try {
            inlineKeyboardButtonJson.put("callback_data", objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return inlineKeyboardButtonJson;
    }

    private JSONObject createButtonMenu(List<JSONObject> list){
        JSONObject mainObj = new JSONObject();
        JSONArray inlineKeyboardButtonArrayJson = new JSONArray();
        list.forEach(button -> inlineKeyboardButtonArrayJson.put(button));
        JSONArray inlineKeyboardArrays = new JSONArray();
        inlineKeyboardArrays.put(inlineKeyboardButtonArrayJson);
        mainObj.put("inline_keyboard", inlineKeyboardArrays);

        return mainObj;
    }
}
