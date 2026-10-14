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

@Component
@RequiredArgsConstructor
public class AddToLearnCallbackHandler implements CallbackHandler {
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final FlashcardsDao flashcardsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Optional<Flashcard> flashcard = flashcardsDao.findFlashcardById(callbackData.entityId());
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        boolean isAdded = userProfileFlashcardsDao.addUserFlashcardIfAbsent(message.getChatId(), flashcard.get());
        String text = isAdded
                ? "Карточка *%s* добавлена для изучения"
                : "Карточка *%s* была ранее добавлена для изучения";
        return List.of(BotMessages.editMarkdown(message, String.format(text, flashcard.get().word())));
    }
}
