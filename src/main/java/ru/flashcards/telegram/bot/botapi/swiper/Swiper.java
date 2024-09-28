package ru.flashcards.telegram.bot.botapi.swiper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.dmlOps.dto.SwiperFlashcard;

import java.util.ArrayList;
import java.util.List;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.*;

public class Swiper {
    private String charCond;
    private String percentile;
    private ObjectMapper objectMapper = new ObjectMapper();
    private SwiperFlashcard swiperFlashcard;

    public Swiper(String charCond, SwiperFlashcard flashcard, String percentile) {
        this.charCond = charCond;
        this.swiperFlashcard = flashcard;
        this.percentile = percentile;
    }

    public InlineKeyboardMarkup getSwiperKeyboardMarkup() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> swiperRowInline = new ArrayList<>();
        List<InlineKeyboardButton> optionsRowInline = new ArrayList<>();
        List<InlineKeyboardButton> removeRowInline = new ArrayList<>();

        try {
            removeRowInline.add(removeButton());

            if (swiperFlashcard.prevId() != 0) {
                swiperRowInline.add(prevButton());
            }
            if (swiperFlashcard.nextId() != 0) {
                swiperRowInline.add(nextButton());
            }
            if (swiperFlashcard.learnPrc() == 100){
                optionsRowInline.add(returnToLearnButton());
            }
            if (swiperFlashcard.learnPrc() == 0 && swiperFlashcard.nearestTraining() == 0){
                optionsRowInline.add(boostPriorityButton());
            }

            optionsRowInline.add(exampleOfUsageButton());


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        rowsInline.add(removeRowInline);
        rowsInline.add(optionsRowInline);
        rowsInline.add(swiperRowInline);
        markupInline.setKeyboard(rowsInline);
        return  markupInline;
    }

    private InlineKeyboardButton prevButton() throws JsonProcessingException {
        InlineKeyboardButton prevButton = new InlineKeyboardButton();
        prevButton.setText("Назад");
        CallbackData prev = new CallbackData(PREV, swiperFlashcard.prevId(), new SwiperParams(charCond, percentile));
        prevButton.setCallbackData(objectMapper.writeValueAsString(prev));

        return prevButton;
    }

    private InlineKeyboardButton nextButton() throws JsonProcessingException {
        InlineKeyboardButton nextButton = new InlineKeyboardButton();
        nextButton.setText("Вперед");
        CallbackData next = new CallbackData(NEXT, swiperFlashcard.nextId(), new SwiperParams(charCond, percentile));
        nextButton.setCallbackData(objectMapper.writeValueAsString(next));

        return nextButton;
    }

    private InlineKeyboardButton returnToLearnButton() throws JsonProcessingException {
        InlineKeyboardButton returnToLearnButton = new InlineKeyboardButton();
        returnToLearnButton.setText("Учить повторно");
        CallbackData returnToLearnCallbackData = new CallbackData(SRELEARN, swiperFlashcard.currentId(), new SwiperParams(charCond, percentile));
        returnToLearnButton.setCallbackData(objectMapper.writeValueAsString(returnToLearnCallbackData));

        return returnToLearnButton;
    }

    private InlineKeyboardButton boostPriorityButton() throws JsonProcessingException {
        InlineKeyboardButton boostPriorityButton = new InlineKeyboardButton();
        boostPriorityButton.setText("Повысить приоритет");
        CallbackData boostPriorityCallbackData = new CallbackData(BOOST, swiperFlashcard.currentId(), new SwiperParams(charCond, percentile));
        boostPriorityButton.setCallbackData(objectMapper.writeValueAsString(boostPriorityCallbackData));

        return boostPriorityButton;
    }

    private InlineKeyboardButton exampleOfUsageButton() throws JsonProcessingException {
        InlineKeyboardButton boostPriorityButton = new InlineKeyboardButton();
        boostPriorityButton.setText("Примеры");
        CallbackData boostPriorityCallbackData = new CallbackData(EXAMPLES, swiperFlashcard.currentId(), new SwiperParams(charCond, percentile));
        boostPriorityButton.setCallbackData(objectMapper.writeValueAsString(boostPriorityCallbackData));

        return boostPriorityButton;
    }

    private InlineKeyboardButton removeButton() throws JsonProcessingException {
        InlineKeyboardButton boostPriorityButton = new InlineKeyboardButton();
        boostPriorityButton.setText("Удалить");
        CallbackData removeFlashcardCallbackData = new CallbackData(REMOVE, swiperFlashcard.currentId(), new SwiperParams(charCond, percentile));
        boostPriorityButton.setCallbackData(objectMapper.writeValueAsString(removeFlashcardCallbackData));

        return boostPriorityButton;
    }
}
