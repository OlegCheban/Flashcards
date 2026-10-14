package ru.flashcards.telegram.bot.botapi.handlers.translation;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARD_NOT_FOUND_MSG;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserMessageTypeBuffer;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;

/**
 * Receives the new translation requested by the {@code /ed} command.
 */
@Component
@RequiredArgsConstructor
public class ChangeTranslationMessageHandler implements MessageHandler {
    /**
     * Size of the user_flashcard.translation column.
     */
    private static final int MAX_TRANSLATION_LENGTH = 255;

    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final UserMessageTypeBuffer userMessageTypeBuffer;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        Long chatId = message.getChatId();
        String translation = message.getText().trim();
        if (translation.isEmpty() || translation.length() > MAX_TRANSLATION_LENGTH) {
            return List.of(BotMessages.text(chatId,
                    "Перевод должен содержать от 1 до " + MAX_TRANSLATION_LENGTH + " символов. Отправьте перевод"));
        }

        Long userFlashcardId = userMessageTypeBuffer.getEntityId(chatId);
        userMessageTypeBuffer.removeRequest(chatId);
        if (userFlashcardId == null
                || !userProfileFlashcardsDao.editTranslation(chatId, userFlashcardId, translation)) {
            return List.of(BotMessages.text(chatId, FLASHCARD_NOT_FOUND_MSG));
        }
        return List.of(BotMessages.text(chatId, "Translation is changed successfully"));
    }
}
