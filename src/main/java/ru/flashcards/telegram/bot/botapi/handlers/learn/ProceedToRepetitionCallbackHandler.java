package ru.flashcards.telegram.bot.botapi.handlers.learn;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARD_NOT_FOUND_MSG;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

/**
 * The user remembers the word from a spaced repetition notification: the flashcard is revealed.
 */
@Component
@RequiredArgsConstructor
public class ProceedToRepetitionCallbackHandler implements CallbackHandler {
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Optional<UserFlashcard> flashcard =
                userProfileFlashcardsDao.findUserFlashcardById(message.getChatId(), callbackData.entityId());
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        UserFlashcard repeated = flashcard.get();
        return List.of(BotMessages.editMarkdown(message, "*Интервальное повторение*\n*" + repeated.word() + "* /"
                + repeated.transcription() + "/\n\n" + repeated.description() + "\n\n" + repeated.translation()));
    }
}
