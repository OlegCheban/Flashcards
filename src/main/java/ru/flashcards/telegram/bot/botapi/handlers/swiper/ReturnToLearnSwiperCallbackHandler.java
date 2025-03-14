package ru.flashcards.telegram.bot.botapi.handlers.swiper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.swiper.Swiper;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.SwiperDao;
import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
@AllArgsConstructor
public class ReturnToLearnSwiperCallbackHandler implements MessageHandler<CallbackQuery> {
    private LearningExercisesDao learningExercisesDao;
    private NotificationsDao notificationsDao;
    private SwiperDao swiperDao;

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

        notificationsDao.deleteSpacedRepetitionHistory(userFlashcardId);
        learningExercisesDao.deleteExerciseStat(userFlashcardId);
        learningExercisesDao.returnToLearn(userFlashcardId);

        SwiperFlashcard swiperFlashcard =
                swiperDao.getSwiperFlashcard(chatId, callbackData.entityId(), characterCondition, percentile);

        EditMessageText formerMessage = new EditMessageText();
        formerMessage.setChatId(String.valueOf(chatId));
        formerMessage.setMessageId(toIntExact(messageId));
        formerMessage.enableMarkdown(true);
        formerMessage.setText("*" + swiperFlashcard.word() + "* /" + swiperFlashcard.transcription() + "/ (" +
                swiperFlashcard.learnPrc()+"% выучено)\n" + swiperFlashcard.description() + "\n\n" +
                "*Перевод:* " + swiperFlashcard.translation()
        );

        Swiper swiper = new Swiper(characterCondition, swiperFlashcard, percentile);
        formerMessage.setReplyMarkup(swiper.getSwiperKeyboardMarkup());
        list.add(formerMessage);

        SendMessage resultMessage = new SendMessage();
        resultMessage.setChatId(String.valueOf(message.getChatId()));
        resultMessage.enableMarkdown(true);
        resultMessage.setText("*" + swiperFlashcard.word() + "* returned to learn");

        list.add(resultMessage);

        return list;
    }
}