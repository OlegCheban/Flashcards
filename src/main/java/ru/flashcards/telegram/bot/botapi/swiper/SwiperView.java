package ru.flashcards.telegram.bot.botapi.swiper;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.BST;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXS;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.NE;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.PR;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.REM;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.SPRON;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.SRTL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;
import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;

/**
 * Renders a flashcard of the Swiper: the card text and its navigation and action buttons.
 * The Swiper filters are carried in every button so that navigation keeps them.
 */
@Component
@RequiredArgsConstructor
public class SwiperView {
    private static final int FULLY_LEARNED_PRC = 100;

    private final InlineKeyboardFactory inlineKeyboardFactory;

    public SendMessage message(Long chatId, SwiperFlashcard flashcard, SwiperParams params) {
        SendMessage message = BotMessages.markdown(chatId, text(flashcard));
        message.setReplyMarkup(keyboard(flashcard, params));
        return message;
    }

    public EditMessageText edit(MaybeInaccessibleMessage message, SwiperFlashcard flashcard, SwiperParams params) {
        EditMessageText editMessage = BotMessages.editMarkdown(message, text(flashcard));
        editMessage.setReplyMarkup(keyboard(flashcard, params));
        return editMessage;
    }

    private String text(SwiperFlashcard flashcard) {
        return "*" + flashcard.word() + "* /" + flashcard.transcription() + "/ (" + flashcard.learnPrc()
                + "% выучено)\n" + flashcard.description() + "\n\n*Перевод:* " + flashcard.translation();
    }

    private InlineKeyboardMarkup keyboard(SwiperFlashcard flashcard, SwiperParams params) {
        Long currentId = flashcard.currentId();

        List<InlineKeyboardButton> optionsRow = new ArrayList<>();
        if (flashcard.learnPrc() == FULLY_LEARNED_PRC) {
            optionsRow.add(button("Учить повторно", SRTL, currentId, params));
        }
        if (flashcard.learnPrc() == 0 && flashcard.nearestTraining() == 0) {
            optionsRow.add(button("Повысить приоритет", BST, currentId, params));
        }
        optionsRow.add(button("Примеры", EXS, currentId, params));
        optionsRow.add(button("Произношение", SPRON, currentId, params));

        List<InlineKeyboardButton> navigationRow = new ArrayList<>();
        if (SwiperFlashcard.exists(flashcard.prevId())) {
            navigationRow.add(button("Назад", PR, flashcard.prevId(), params));
        }
        if (SwiperFlashcard.exists(flashcard.nextId())) {
            navigationRow.add(button("Вперед", NE, flashcard.nextId(), params));
        }

        return inlineKeyboardFactory.rows(List.of(
                List.of(button("Удалить", REM, currentId, params)),
                optionsRow,
                navigationRow));
    }

    private InlineKeyboardButton button(String text, BotKeyboardButton command, Long flashcardId, SwiperParams params) {
        return inlineKeyboardFactory.button(text, new CallbackData(command, flashcardId, params));
    }
}
