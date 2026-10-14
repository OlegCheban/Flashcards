package ru.flashcards.telegram.bot.botapi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

/**
 * Factory methods for outgoing Telegram messages.
 */
public final class BotMessages {
    /**
     * Telegram's limit for the text of a message.
     */
    public static final int MAX_TEXT_LENGTH = 4096;

    private static final Pattern MARKDOWN_SPECIAL_CHARACTERS = Pattern.compile("([_*`\\[])");

    private BotMessages() {
    }

    /**
     * Escapes user input embedded into a legacy Markdown message; otherwise Telegram rejects the whole message.
     */
    public static String escapeMarkdown(String text) {
        return MARKDOWN_SPECIAL_CHARACTERS.matcher(text).replaceAll("\\\\$1");
    }

    /**
     * Splits a long plain text into parts that fit into a single message, preferably at line breaks.
     */
    public static List<String> splitText(String text) {
        List<String> parts = new ArrayList<>();
        String rest = text;
        while (rest.length() > MAX_TEXT_LENGTH) {
            int splitAt = rest.lastIndexOf('\n', MAX_TEXT_LENGTH);
            if (splitAt <= 0) {
                splitAt = MAX_TEXT_LENGTH;
            }
            parts.add(rest.substring(0, splitAt));
            rest = rest.substring(splitAt).stripLeading();
        }
        if (!rest.isEmpty()) {
            parts.add(rest);
        }
        return parts;
    }

    public static SendMessage text(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    public static SendMessage markdown(Long chatId, String text) {
        SendMessage sendMessage = text(chatId, text);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    public static SendMessage withoutKeyboard(SendMessage sendMessage) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);
        return sendMessage;
    }

    public static EditMessageText editText(MaybeInaccessibleMessage message, String text) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(message.getMessageId());
        editMessage.setText(text);
        return editMessage;
    }

    public static EditMessageText editMarkdown(MaybeInaccessibleMessage message, String text) {
        EditMessageText editMessage = editText(message, text);
        editMessage.setParseMode(ParseMode.MARKDOWN);
        return editMessage;
    }
}
