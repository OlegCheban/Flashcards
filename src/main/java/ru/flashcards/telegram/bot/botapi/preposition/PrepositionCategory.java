package ru.flashcards.telegram.bot.botapi.preposition;

import java.util.Locale;
import java.util.Optional;

/**
 * Topic of preposition exercises; {@link #code()} is the argument of the {@code /pr} command.
 */
public enum PrepositionCategory {
    ADJECTIVE("adj", "Прилагательные + предлог"),
    NOUN("nouns", "Существительные + предлог"),
    VERB("verbs", "Глаголы + предлог"),
    TIME("time", "Время"),
    PLACE("place", "Место и движение"),
    PHRASE("phrases", "Устойчивые выражения"),
    BUSINESS("business", "Деловой английский");

    private final String code;
    private final String title;

    PrepositionCategory(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public String code() {
        return code;
    }

    public String title() {
        return title;
    }

    public static Optional<PrepositionCategory> fromCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);
        for (PrepositionCategory category : values()) {
            if (category.code.equals(normalizedCode)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
