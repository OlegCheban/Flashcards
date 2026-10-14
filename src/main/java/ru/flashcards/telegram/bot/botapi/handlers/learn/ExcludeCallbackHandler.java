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
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.Flashcard;

/**
 * The user already knows the flashcard: it is not suggested any more.
 */
@Component
@RequiredArgsConstructor
public class ExcludeCallbackHandler implements CallbackHandler {
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final FlashcardsDao flashcardsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Long flashcardId = callbackData.entityId();
        Optional<Flashcard> flashcard = flashcardsDao.findFlashcardById(flashcardId);
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        userProfileFlashcardsDao.exceptFlashcard(message.getChatId(), flashcardId);
        return List.of(BotMessages.editMarkdown(message, "Карточка *" + flashcard.get().word()
                + "* успешно исключена. Бот больше не будет Вам ее предлагать"));
    }
}
