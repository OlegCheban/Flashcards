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
import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;

/**
 * Shows the previous or the next flashcard of the Swiper.
 */
@Component
@RequiredArgsConstructor
public class SwiperRefreshFlashcardCallbackHandler implements CallbackHandler {
    private final SwiperDao swiperDao;
    private final SwiperView swiperView;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        SwiperParams params = callbackData.swiper();
        Optional<SwiperFlashcard> flashcard = swiperDao.getSwiperFlashcard(
                message.getChatId(), callbackData.entityId(), params.charCond(), params.prc());
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }
        return List.of(swiperView.edit(message, flashcard.get(), params));
    }
}
