package ru.flashcards.telegram.bot.services;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXS;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.SPRON;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.TRANSLATE;

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
import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;

/**
 * Reminds users of a random flashcard from their profile together with an AI generated usage context.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RandomNotificationService {

    private static final String PUSHPIN_EMOJI = "\uD83D\uDCCC";

    private final NotificationsDao notificationsDao;
    private final SendMessageService sendMessageService;
    private final FlashcardsContextService flashcardsContextService;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    /**
     * A failure for one user is logged and does not stop notifications for the others.
     */
    public void send() {
        for (UserFlashcardPushMono flashcard : notificationsDao.getUserFlashcardsRandomNotification()) {
            try {
                sendNotification(flashcard);
            } catch (RuntimeException e) {
                log.error("Random notification failed: userFlashcardId={}", flashcard.userFlashcardId(), e);
            }
        }
    }

    private void sendNotification(UserFlashcardPushMono flashcard) {
        SendMessage message = BotMessages.markdown(flashcard.userId(), buildNotificationText(flashcard));
        message.setReplyMarkup(createNotificationKeyboard(flashcard.userFlashcardId()));
        sendMessageService.send(message);

        notificationsDao.updatePushTimestamp(flashcard.userFlashcardId());
    }

    private String buildNotificationText(UserFlashcardPushMono flashcard) {
        String context = flashcardsContextService.generateContext(flashcard.word());

        return String.format("*%s* /%s/ %s\n\n%s\n\n%s",
                flashcard.word(),
                flashcard.transcription(),
                PUSHPIN_EMOJI,
                flashcard.description(),
                context);
    }

    private InlineKeyboardMarkup createNotificationKeyboard(Long userFlashcardId) {
        return inlineKeyboardFactory.row(List.of(
                inlineKeyboardFactory.button("Перевод", new CallbackData(TRANSLATE, userFlashcardId)),
                inlineKeyboardFactory.button("Примеры", new CallbackData(EXS, userFlashcardId)),
                inlineKeyboardFactory.button("Слушать произношение", new CallbackData(SPRON, userFlashcardId))));
    }
}
