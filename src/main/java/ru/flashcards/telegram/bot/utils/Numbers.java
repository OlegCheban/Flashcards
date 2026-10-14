package ru.flashcards.telegram.bot.utils;

import java.util.OptionalInt;

public final class Numbers {

    private Numbers() {
    }

    /**
     * @return {@code true} if the text is a non-empty sequence of ASCII digits
     */
    public static boolean isDigits(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character < '0' || character > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the number if the text is a positive integer that fits into {@code int}, otherwise empty
     */
    public static OptionalInt parsePositiveInt(String text) {
        if (!isDigits(text)) {
            return OptionalInt.empty();
        }
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? OptionalInt.of(value) : OptionalInt.empty();
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
