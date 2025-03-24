package ru.flashcards.telegram.bot.services;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;

import java.util.ArrayList;
import java.util.List;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXS;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.TRANSLATE;

@Service
@AllArgsConstructor
public class RandomNotificationService {

    private static final String PUSHPIN_EMOJI = "\uD83D\uDCCC";
    private final NotificationsDao notificationsDao;
    private final SendMessageService sendMessageService;

    public void send() {
        List<UserFlashcardPushMono> flashcardsToNotify = getFlashcardsForNotification();
        flashcardsToNotify.forEach(this::sendNotification);
    }

    private List<UserFlashcardPushMono> getFlashcardsForNotification() {
        return notificationsDao.getUserFlashcardsRandomNotification();
    }

    private void sendNotification(UserFlashcardPushMono flashcard) {
        String message = buildNotificationMessage(flashcard);
        String inlineKeyboard = createNotificationKeyboard(flashcard.userFlashcardId());

        sendMessageService.sendMessage(
                flashcard.userId(),
                message,
                inlineKeyboard
        );

        updateNotificationTimestamp(flashcard.userFlashcardId());
    }

    private String buildNotificationMessage(UserFlashcardPushMono flashcard) {
        return String.format("*%s* /%s/ %s%n%n%s",
                flashcard.word(),
                flashcard.transcription(),
                PUSHPIN_EMOJI,
                flashcard.description());
    }

    private String createNotificationKeyboard(Long userFlashcardId) {
        List<JSONObject> buttons = new ArrayList<>();
        buttons.add(InlineKeyboardCreator.prepareButton(userFlashcardId, "Перевод", TRANSLATE));
        buttons.add(InlineKeyboardCreator.prepareButton(userFlashcardId, "Примеры", EXS));
        return String.valueOf(InlineKeyboardCreator.createButtonMenu(buttons));
    }

    private void updateNotificationTimestamp(Long userFlashcardId) {
        notificationsDao.updatePushTimestamp(userFlashcardId);
    }
}
