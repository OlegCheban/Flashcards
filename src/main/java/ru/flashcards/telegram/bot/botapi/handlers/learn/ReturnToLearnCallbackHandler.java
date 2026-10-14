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
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;
import ru.flashcards.telegram.bot.services.UserFlashcardService;

/**
 * The user does not remember the word from a spaced repetition notification: the flashcard is learned again.
 */
@Component
@RequiredArgsConstructor
public class ReturnToLearnCallbackHandler implements CallbackHandler {
    private final UserFlashcardService userFlashcardService;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Optional<UserFlashcard> flashcard =
                userFlashcardService.returnToLearn(message.getChatId(), callbackData.entityId());
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        UserFlashcard returned = flashcard.get();
        return List.of(BotMessages.editMarkdown(message, "*Карточка возвращена для повторного изучения*\n*"
                + returned.word() + "* /" + returned.transcription() + "/\n\n" + returned.description()
                + "\n\n" + returned.translation()));
    }
}
