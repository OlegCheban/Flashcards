package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks the phrasal verb content; all problems are reported at once on start.
 * Definitions must be unique: an equal definition of another verb would be a second correct option.
 */
final class PhrasalVerbValidator {
    static final int MAX_DEFINITION_LENGTH = 100;
    static final int MAX_EXAMPLE_LENGTH = 200;

    /**
     * The verb is shown in bold, so it must not contain Markdown characters.
     */
    private static final Pattern VERB_PATTERN = Pattern.compile("[a-z]+( [a-z]+)*( / [a-z]+( [a-z]+)*)?");
    private static final Pattern SENSE_PATTERN = Pattern.compile("[a-z]+(-[a-z]+)*");

    private PhrasalVerbValidator() {
    }

    static void validate(List<PhrasalVerb> verbs) {
        List<String> errors = new ArrayList<>();
        Set<String> verbNames = new HashSet<>();
        Set<String> definitions = new HashSet<>();
        for (PhrasalVerb verb : verbs) {
            List<String> verbErrors = new ArrayList<>();
            if (verb.verb() == null || !VERB_PATTERN.matcher(verb.verb()).matches()) {
                verbErrors.add("verb must be lowercase words");
            } else if (!verbNames.add(verb.verb())) {
                verbErrors.add("duplicate verb");
            }
            if (verb.meanings().isEmpty()) {
                verbErrors.add("no meanings");
            }
            for (PhrasalVerbMeaning meaning : verb.meanings()) {
                validateMeaning(meaning, definitions, verbErrors);
            }
            for (String error : verbErrors) {
                errors.add("\"" + verb.verb() + "\": " + error);
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid phrasal verbs:\n" + String.join("\n", errors));
        }
    }

    private static void validateMeaning(PhrasalVerbMeaning meaning, Set<String> definitions, List<String> errors) {
        String definition = meaning.definition();
        if (definition == null || definition.isBlank()) {
            errors.add("definition is missing");
        } else {
            if (definition.length() > MAX_DEFINITION_LENGTH) {
                errors.add("definition is longer than " + MAX_DEFINITION_LENGTH + ": " + definition);
            }
            if (!definitions.add(definition.toLowerCase(Locale.ROOT))) {
                errors.add("duplicate definition: " + definition);
            }
        }

        if (meaning.senses().isEmpty()) {
            errors.add("no senses: " + definition);
        }
        for (String sense : meaning.senses()) {
            if (sense == null || !SENSE_PATTERN.matcher(sense).matches()) {
                errors.add("sense must be lowercase words joined with '-': " + sense);
            }
        }

        if (meaning.example() == null || meaning.example().isBlank()) {
            errors.add("example is missing: " + definition);
        } else if (meaning.example().length() > MAX_EXAMPLE_LENGTH) {
            errors.add("example is longer than " + MAX_EXAMPLE_LENGTH + ": " + definition);
        }
    }
}
