package ru.flashcards.telegram.bot.botapi.handlers.swiper;

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
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.botapi.swiper.SwiperView;
import ru.flashcards.telegram.bot.db.SwiperDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;

/**
 * Moves the flashcard to the next learning session.
 */
@Component
@RequiredArgsConstructor
public class BoostPriorityCallbackHandler implements CallbackHandler {
    private final SwiperDao swiperDao;
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final SwiperView swiperView;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        Long userFlashcardId = callbackData.entityId();
        SwiperParams params = callbackData.swiper();

        if (!userProfileFlashcardsDao.boostUserFlashcardPriority(chatId, userFlashcardId)) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        Optional<SwiperFlashcard> flashcard =
                swiperDao.getSwiperFlashcard(chatId, userFlashcardId, params.charCond(), params.prc());
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        return List.of(
                swiperView.edit(message, flashcard.get(), params),
                BotMessages.markdown(chatId, "*" + flashcard.get().word() + "* added to next learning session"));
    }
}
