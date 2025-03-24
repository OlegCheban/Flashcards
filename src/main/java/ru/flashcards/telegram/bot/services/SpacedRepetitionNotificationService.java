package ru.flashcards.telegram.bot.services;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardSpacedRepetitionNotification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.PROCEED;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.RTL;

@Service
@AllArgsConstructor
public class SpacedRepetitionNotificationService {

    private static final String ALARM_EMOJI = "\u23F0";
    private static final String REMEMBER_PROMPT = "Попытайтесь вспомнить выученное слово. Нажмите да если помните";

    private final SendMessageService sendMessageService;
    private final NotificationsDao notificationsDao;

    public void send() {
        refreshPushQueueIfNeeded();
        processDueNotifications();
    }

    private void refreshPushQueueIfNeeded() {
        if (!notificationsDao.isPushQueueUpToCurrentDate()) {
            notificationsDao.refreshIntervalNotification();
        }
    }

    private void processDueNotifications() {
        notificationsDao.getUserFlashcardsSpacedRepetitionNotification()
                .stream()
                .filter(this::isNotificationDue)
                .forEach(this::sendSpacedRepetitionNotification);
    }

    private boolean isNotificationDue(UserFlashcardSpacedRepetitionNotification notification) {
        return notification.notificationDate().isBefore(LocalDateTime.now());
    }

    private void sendSpacedRepetitionNotification(UserFlashcardSpacedRepetitionNotification notification) {
        String message = buildNotificationMessage(notification);
        String inlineKeyboard = createConfirmationKeyboard(notification.userFlashcardId());

        sendMessageService.sendMessage(
                notification.userId(),
                message,
                inlineKeyboard
        );

        recordNotificationSent(notification.userFlashcardId());
    }

    private String buildNotificationMessage(UserFlashcardSpacedRepetitionNotification notification) {
        return String.format(
                "*Интервальное повторение* %s\n*%s* /%s/ (%d%% выучено)\n\n%s",
                ALARM_EMOJI,
                notification.word(),
                notification.transcription(),
                notification.prc(),
                REMEMBER_PROMPT
        );
    }

    private String createConfirmationKeyboard(Long userFlashcardId) {
        List<JSONObject> buttons = Arrays.asList(
                InlineKeyboardCreator.prepareButton(userFlashcardId, "Да", PROCEED),
                InlineKeyboardCreator.prepareButton(userFlashcardId, "Нет", RTL)
        );
        return String.valueOf(InlineKeyboardCreator.createButtonMenu(buttons));
    }

    private void recordNotificationSent(Long userFlashcardId) {
        notificationsDao.addFlashcardPushHistory(userFlashcardId);
    }
}
