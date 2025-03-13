package ru.flashcards.telegram.bot.botapi.handlers.swiper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.swiper.Swiper;
import ru.flashcards.telegram.bot.db.dmlOps.SwiperDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.SwiperFlashcard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
@AllArgsConstructor
public class SwiperRefreshFlashcardCallbackHandler implements MessageHandler<CallbackQuery> {
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

        if (callbackData.swiper() != null){
            characterCondition = callbackData.swiper().charCond();
            percentile = callbackData.swiper().prc();
        }
        SwiperFlashcard swiperFlashcard = swiperDao.getSwiperFlashcard(chatId, callbackData.entityId(), characterCondition, percentile);

        EditMessageText nextMessage = new EditMessageText();
        nextMessage.setChatId(String.valueOf(chatId));
        nextMessage.setMessageId(toIntExact(messageId));
        nextMessage.enableMarkdown(true);
        nextMessage.setText("*" + swiperFlashcard.word() + "* /" + swiperFlashcard.transcription() + "/ ("+swiperFlashcard.learnPrc()+"% выучено)\n" +
                swiperFlashcard.description() + "\n\n" + "*Перевод:* " + swiperFlashcard.translation()
        );

        Swiper swiper = new Swiper(characterCondition, swiperFlashcard, percentile);

        nextMessage.setReplyMarkup(swiper.getSwiperKeyboardMarkup());
        list.add(nextMessage);

        return list;
    }
}