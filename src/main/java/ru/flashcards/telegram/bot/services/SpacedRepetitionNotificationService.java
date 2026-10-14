package ru.flashcards.telegram.bot.services;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.PROCEED;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.RTL;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardSpacedRepetitionNotification;

/**
 * Asks users to recall learned flashcards according to the spaced repetition schedule.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionNotificationService {

    private static final String ALARM_EMOJI = "\u23F0";
    private static final String REMEMBER_PROMPT = "Попытайтесь вспомнить выученное слово. Нажмите да если помните";

    private final SendMessageService sendMessageService;
    private final NotificationsDao notificationsDao;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final Clock clock;

    public void send() {
        refreshPushQueueIfNeeded();
        processDueNotifications();
    }

    private void refreshPushQueueIfNeeded() {
        if (!notificationsDao.isPushQueueUpToCurrentDate()) {
            notificationsDao.refreshIntervalNotification();
        }
    }

    /**
     * A failure for one flashcard is logged and does not stop the remaining notifications.
     */
    private void processDueNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);
        for (UserFlashcardSpacedRepetitionNotification notification
                : notificationsDao.getUserFlashcardsSpacedRepetitionNotification()) {
            if (!notification.notificationDate().isBefore(now)) {
                continue;
            }
            try {
                sendSpacedRepetitionNotification(notification);
            } catch (RuntimeException e) {
                log.error("Spaced repetition notification failed: userFlashcardId={}",
                        notification.userFlashcardId(), e);
            }
        }
    }

    private void sendSpacedRepetitionNotification(UserFlashcardSpacedRepetitionNotification notification) {
        SendMessage message = BotMessages.markdown(notification.userId(), buildNotificationText(notification));
        message.setReplyMarkup(createConfirmationKeyboard(notification.userFlashcardId()));
        sendMessageService.send(message);

        notificationsDao.addFlashcardPushHistory(notification.userFlashcardId());
    }

    private String buildNotificationText(UserFlashcardSpacedRepetitionNotification notification) {
        return String.format(
                "*Интервальное повторение* %s\n*%s* /%s/ (%d%% выучено)\n\n%s",
                ALARM_EMOJI,
                notification.word(),
                notification.transcription(),
                notification.prc(),
                REMEMBER_PROMPT
        );
    }

    private InlineKeyboardMarkup createConfirmationKeyboard(Long userFlashcardId) {
        return inlineKeyboardFactory.row(List.of(
                inlineKeyboardFactory.button("Да", new CallbackData(PROCEED, userFlashcardId)),
                inlineKeyboardFactory.button("Нет", new CallbackData(RTL, userFlashcardId))));
    }
}
