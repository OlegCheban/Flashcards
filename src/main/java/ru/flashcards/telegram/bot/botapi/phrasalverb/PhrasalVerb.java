package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A phrasal verb with all its meanings.
 */
public record PhrasalVerb(String verb, List<PhrasalVerbMeaning> meanings) {

    public PhrasalVerb {
        meanings = meanings == null ? List.of() : List.copyOf(meanings);
    }

    /**
     * Semantic labels of all meanings.
     */
    public Set<String> senses() {
        Set<String> senses = new HashSet<>();
        for (PhrasalVerbMeaning meaning : meanings) {
            senses.addAll(meaning.senses());
        }
        return senses;
    }
}
