package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.util.List;

import ru.flashcards.telegram.bot.botapi.training.TrainingQuestion;

/**
 * "Choose the meaning of the phrasal verb": numbered options, one of them is the asked meaning.
 * The user answers with the option number or with the definition itself.
 */
public record PhrasalVerbQuestion(PhrasalVerb verb, PhrasalVerbMeaning meaning, List<String> options)
        implements TrainingQuestion {

    public PhrasalVerbQuestion {
        options = List.copyOf(options);
        if (!options.contains(meaning.definition())) {
            throw new IllegalArgumentException("Options must contain the correct meaning: verb=" + verb.verb());
        }
    }

    /**
     * @return the 1-based number of the correct option
     */
    public int correctOptionNumber() {
        return options.indexOf(meaning.definition()) + 1;
    }

    @Override
    public boolean isCorrect(String answer) {
        String trimmedAnswer = answer.trim();
        return trimmedAnswer.equals(String.valueOf(correctOptionNumber()))
                || trimmedAnswer.equalsIgnoreCase(meaning.definition());
    }
}
