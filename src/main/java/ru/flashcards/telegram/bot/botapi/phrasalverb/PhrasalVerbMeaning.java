package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.util.List;

/**
 * One meaning of a phrasal verb.
 *
 * @param definition the meaning in English, shown as an answer option
 * @param senses     short semantic labels; meanings of different verbs that could be taken for one another
 *                   share a label, so one of them is never offered as a wrong option for the other
 * @param example    a sentence that uses the phrasal verb in this meaning
 */
public record PhrasalVerbMeaning(String definition, List<String> senses, String example) {

    public PhrasalVerbMeaning {
        senses = senses == null ? List.of() : List.copyOf(senses);
    }
}
