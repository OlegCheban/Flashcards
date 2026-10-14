package ru.flashcards.telegram.bot.botapi.keyboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.CallbackDataCodec;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;

@Component
@RequiredArgsConstructor
public class InlineKeyboardFactory {
    private final CallbackDataCodec callbackDataCodec;

    public InlineKeyboardButton button(String text, CallbackData callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackDataCodec.encode(callbackData));
        return button;
    }

    /**
     * All buttons in a single row.
     */
    public InlineKeyboardMarkup row(List<InlineKeyboardButton> buttons) {
        return rows(List.of(buttons));
    }

    /**
     * Every button in its own row.
     */
    public InlineKeyboardMarkup column(List<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (InlineKeyboardButton button : buttons) {
            rows.add(List.of(button));
        }
        return rows(rows);
    }

    /**
     * Empty rows are skipped because Telegram rejects them.
     */
    public InlineKeyboardMarkup rows(List<List<InlineKeyboardButton>> rows) {
        List<List<InlineKeyboardButton>> nonEmptyRows = new ArrayList<>();
        for (List<InlineKeyboardButton> row : rows) {
            if (!row.isEmpty()) {
                nonEmptyRows.add(row);
            }
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(nonEmptyRows);
        return markup;
    }
}
