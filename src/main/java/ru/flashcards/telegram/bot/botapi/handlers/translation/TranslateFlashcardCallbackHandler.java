package ru.flashcards.telegram.bot.botapi.handlers.translation;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXS;
import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARD_NOT_FOUND_MSG;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

/**
 * Reveals the translation of a flashcard from a random notification.
 */
@Component
@RequiredArgsConstructor
public class TranslateFlashcardCallbackHandler implements CallbackHandler {
    private static final String PUSHPIN_EMOJI = "\uD83D\uDCCC";

    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Long userFlashcardId = callbackData.entityId();
        Optional<UserFlashcard> flashcard =
                userProfileFlashcardsDao.findUserFlashcardById(message.getChatId(), userFlashcardId);
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        UserFlashcard translated = flashcard.get();
        EditMessageText translationMessage = BotMessages.editMarkdown(message, "*" + translated.word() + "* /"
                + translated.transcription() + "/ " + PUSHPIN_EMOJI + " \n" + translated.description()
                + "\n\n" + translated.translation());
        translationMessage.setReplyMarkup(inlineKeyboardFactory.row(List.of(
                inlineKeyboardFactory.button("example of usage", new CallbackData(EXS, userFlashcardId)))));
        return List.of(translationMessage);
    }
}
