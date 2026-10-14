package ru.flashcards.telegram.bot.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomMessageText {

    private static final List<String> POSITIVE_MESSAGES = List.of(
            "Brilliant!",
            "Wonderful!",
            "Keep it up!",
            "Fantastic!",
            "Excellent!",
            "Superb!",
            "Spectacular!",
            "Outstanding!",
            "Awesome!",
            "Keep going!",
            "Amazing!",
            "That's right!",
            "Astonishing!",
            "Well done!",
            "Magnificent!"
    );

    private static final List<String> NEGATIVE_MESSAGES = List.of(
            "Wrong",
            "Incorrect",
            "Mistake",
            "Improperly",
            "Miss",
            "Failure",
            "Bad"
    );

    private RandomMessageText() {
    }

    public static String getPositiveMessage() {
        return randomOf(POSITIVE_MESSAGES);
    }

    public static String getNegativeMessage() {
        return randomOf(NEGATIVE_MESSAGES);
    }

    private static String randomOf(List<String> messages) {
        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }
}
