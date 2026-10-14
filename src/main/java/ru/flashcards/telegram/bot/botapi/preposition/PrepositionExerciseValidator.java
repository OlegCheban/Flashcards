package ru.flashcards.telegram.bot.botapi.preposition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Checks the exercise content: a wrong exercise would mark a correct answer as a mistake
 * or break the Markdown of the message, so all problems are reported at once on start.
 */
final class PrepositionExerciseValidator {
    static final int MIN_DISTRACTORS = 3;
    static final int MAX_SENTENCE_LENGTH = 200;
    static final int MAX_EXPLANATION_LENGTH = 300;

    /**
     * Words allowed as answers and distractors, catches typos in the content.
     * {@code than} and {@code while} are not prepositions but typical learner mistakes,
     * so they are allowed as distractors.
     */
    private static final Set<String> KNOWN_WORDS = Set.of(
            "about", "above", "across", "after", "against", "along", "among", "around", "as", "at",
            "before", "behind", "below", "between", "beyond", "by", "down", "during", "for", "from",
            "in", "inside", "into", "near", "of", "off", "on", "onto", "out", "out of", "over", "since",
            "than", "through", "till", "to", "toward", "towards", "under", "until", "up", "upon", "while",
            "with", "within", "without");

    /**
     * Characters with a special meaning in Telegram's legacy Markdown; {@code *} is allowed for bold words only.
     */
    private static final String MARKDOWN_SPECIAL_CHARACTERS = "_`[";

    private PrepositionExerciseValidator() {
    }

    static void validate(List<PrepositionExercise> exercises) {
        List<String> errors = new ArrayList<>();
        Set<String> sentences = new HashSet<>();
        for (PrepositionExercise exercise : exercises) {
            List<String> exerciseErrors = new ArrayList<>();
            validateSentence(exercise.sentence(), exerciseErrors);
            validateAnswers(exercise, exerciseErrors);
            validateExplanation(exercise.explanation(), exerciseErrors);
            if (exercise.category() == null) {
                exerciseErrors.add("category is missing");
            }
            if (exercise.sentence() != null && !sentences.add(exercise.sentence().toLowerCase(Locale.ROOT))) {
                exerciseErrors.add("duplicate sentence");
            }
            for (String error : exerciseErrors) {
                errors.add("\"" + exercise.sentence() + "\": " + error);
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid preposition exercises:\n" + String.join("\n", errors));
        }
    }

    private static void validateSentence(String sentence, List<String> errors) {
        if (sentence == null || sentence.isBlank()) {
            errors.add("sentence is missing");
            return;
        }
        if (sentence.indexOf(PrepositionExercise.GAP) < 0
                || sentence.indexOf(PrepositionExercise.GAP) != sentence.lastIndexOf(PrepositionExercise.GAP)) {
            errors.add("sentence must contain exactly one gap " + PrepositionExercise.GAP);
        }
        if (sentence.chars().filter(character -> character == '*').count() % 2 != 0) {
            errors.add("unbalanced * in sentence");
        }
        for (char character : MARKDOWN_SPECIAL_CHARACTERS.toCharArray()) {
            if (sentence.indexOf(character) >= 0) {
                errors.add("sentence contains Markdown character " + character);
            }
        }
        if (sentence.length() > MAX_SENTENCE_LENGTH) {
            errors.add("sentence is longer than " + MAX_SENTENCE_LENGTH);
        }
    }

    private static void validateAnswers(PrepositionExercise exercise, List<String> errors) {
        if (exercise.answers().isEmpty()) {
            errors.add("no answers");
        }
        if (exercise.distractors().size() < MIN_DISTRACTORS) {
            errors.add("less than " + MIN_DISTRACTORS + " distractors");
        }
        validateWords(exercise.answers(), "answer", errors);
        validateWords(exercise.distractors(), "distractor", errors);
        for (String distractor : exercise.distractors()) {
            if (exercise.answers().contains(distractor)) {
                errors.add("distractor is also an answer: " + distractor);
            }
        }
    }

    private static void validateWords(List<String> words, String kind, List<String> errors) {
        Set<String> uniqueWords = new HashSet<>();
        for (String word : words) {
            if (!KNOWN_WORDS.contains(word)) {
                errors.add("unknown " + kind + ": " + word);
            }
            if (!uniqueWords.add(word)) {
                errors.add("duplicate " + kind + ": " + word);
            }
        }
    }

    private static void validateExplanation(String explanation, List<String> errors) {
        if (explanation == null || explanation.isBlank()) {
            errors.add("explanation is missing");
        } else if (explanation.length() > MAX_EXPLANATION_LENGTH) {
            errors.add("explanation is longer than " + MAX_EXPLANATION_LENGTH);
        }
    }
}
