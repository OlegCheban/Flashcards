package ru.flashcards.telegram.bot.sheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardSpacedRepetitionNotification;
import ru.flashcards.telegram.bot.service.InlineKeyboardCreator;
import ru.flashcards.telegram.bot.service.SendService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.*;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.RTL;

@Component
public class NotificationManager {
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
            listButtons.add(InlineKeyboardCreator.prepareButton(queue.userFlashcardId(), "Перевод", TRANSLATE));
            listButtons.add(InlineKeyboardCreator.prepareButton(queue.userFlashcardId(), "Примеры", EXS));
            sendService.sendMessage(
                    queue.userId(),
                    "*"+queue.word()+"* /" + queue.transcription() + "/ " + pushpinEmoji + "\n\n"+queue.description(),
                    String.valueOf(InlineKeyboardCreator.createButtonMenu(listButtons))
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
                listButtons.add(InlineKeyboardCreator.prepareButton(queue.userFlashcardId(), "Да", PROCEED));
                listButtons.add(InlineKeyboardCreator.prepareButton(queue.userFlashcardId(), "Нет", RTL));

                sendService.sendMessage(queue.userId(),
                        "*Интервальное повторение* " + alarmEmoji +
                                "\n*" + queue.word()+ "* /" + queue.transcription() + "/ ("+queue.prc()+"% выучено)" +
                                "\n\n Попытайтесь вспомнить выученное слово. Нажмите да если помните",
                        String.valueOf(InlineKeyboardCreator.createButtonMenu(listButtons)));
                notificationsDao.addFlashcardPushHistory(queue.userFlashcardId());
            }
        });
    }


}
