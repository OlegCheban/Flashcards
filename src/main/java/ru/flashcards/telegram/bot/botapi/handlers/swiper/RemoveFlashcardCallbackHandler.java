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
import ru.flashcards.telegram.bot.services.UserFlashcardService;

/**
 * Removes the flashcard from the user's profile and shows a neighbouring one, if any.
 */
@Component
@RequiredArgsConstructor
public class RemoveFlashcardCallbackHandler implements CallbackHandler {
    private static final String FLASHCARD_REMOVED_MSG = "The flashcard was removed";

    private final UserFlashcardService userFlashcardService;
    private final SwiperDao swiperDao;
    private final SwiperView swiperView;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        SwiperParams params = callbackData.swiper();

        Optional<SwiperFlashcard> removed =
                swiperDao.getSwiperFlashcard(chatId, callbackData.entityId(), params.charCond(), params.prc());
        if (removed.isEmpty() || !userFlashcardService.remove(chatId, callbackData.entityId())) {
            return List.of(BotMessages.editText(message, FLASHCARD_NOT_FOUND_MSG));
        }

        SwiperFlashcard removedFlashcard = removed.get();
        Long neighbourId = SwiperFlashcard.exists(removedFlashcard.nextId())
                ? removedFlashcard.nextId()
                : removedFlashcard.prevId();
        if (!SwiperFlashcard.exists(neighbourId)) {
            return List.of(BotMessages.editText(message, FLASHCARD_REMOVED_MSG));
        }

        return swiperDao.getSwiperFlashcard(chatId, neighbourId, params.charCond(), params.prc())
                .<List<BotApiMethod<?>>>map(neighbour -> List.of(swiperView.edit(message, neighbour, params)))
                .orElseGet(() -> List.of(BotMessages.editText(message, FLASHCARD_REMOVED_MSG)));
    }
}
