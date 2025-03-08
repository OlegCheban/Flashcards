package ru.flashcards.telegram.bot.botapi.handlers.swiper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.swiper.Swiper;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.SwiperFlashcard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
@AllArgsConstructor
public class RemoveFlashcardCallbackHandler implements MessageHandler<CallbackQuery> {
    private DataLayerObject dataLayer;
    private LearningExercisesDao learningExercisesDao;
    private FlashcardsDao flashcardsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        String characterCondition = null;
        String percentile = null;
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        long messageId = message.getMessageId();
        long chatId = message.getChatId();
        Long userFlashcardId = callbackData.entityId();

        if (callbackData.swiper() != null){
            characterCondition = callbackData.swiper().charCond();
            percentile = callbackData.swiper().prc();
        }

        SwiperFlashcard swiperFlashcard =
                dataLayer.getSwiperFlashcard(
                        chatId,
                        callbackData.entityId(),
                        characterCondition,
                        percentile
                );

        dataLayer.deleteSpacedRepetitionHistory(userFlashcardId);
        learningExercisesDao.deleteExerciseStat(userFlashcardId);
        flashcardsDao.removeFlashcard(userFlashcardId);

        EditMessageText formerMessage = new EditMessageText();
        formerMessage.setChatId(String.valueOf(chatId));
        formerMessage.setMessageId(toIntExact(messageId));
        formerMessage.enableMarkdown(true);

        if (swiperFlashcard.prevId() != 0 || swiperFlashcard.nextId() != 0){
            swiperFlashcard =
                    dataLayer.getSwiperFlashcard(
                            chatId,
                            (swiperFlashcard.nextId() == 0) ? swiperFlashcard.prevId() : swiperFlashcard.nextId(),
                            characterCondition,
                            percentile
                    );

            formerMessage.setText("*" + swiperFlashcard.word() + "* /" + swiperFlashcard.transcription() + "/ (" +
                    swiperFlashcard.learnPrc()+"% выучено)\n" + swiperFlashcard.description() + "\n\n" +
                    "*Перевод:* " + swiperFlashcard.translation()
            );

            Swiper swiper = new Swiper(characterCondition, swiperFlashcard, percentile);
            formerMessage.setReplyMarkup(swiper.getSwiperKeyboardMarkup());
        } else {
            formerMessage.setText("The flashcard was removed");
        }
        list.add(formerMessage);

        return list;
    }
}
