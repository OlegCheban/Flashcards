package ru.flashcards.telegram.bot.botapi.preposition;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class PrepositionCatalogTest {

    @Test
    void loadsValidExercisesWithEnoughQuestionsForEveryTopic() throws IOException {
        PrepositionCatalog catalog = loadRealCatalog();

        for (PrepositionCategory category : PrepositionCategory.values()) {
            int quantity = catalog.byCategory(category).size();
            assertTrue(quantity >= PrepositionLearningMode.SESSION_SIZE, category + " has only " + quantity);
        }
    }

    @Test
    void acceptsValidExercise() {
        assertDoesNotThrow(() -> new PrepositionCatalog(List.of(exercise("I'm *good* ... chess.", "at"))));
    }

    @Test
    void rejectsDistractorThatIsAlsoAnAnswer() {
        PrepositionExercise exercise = new PrepositionExercise(PrepositionCategory.ADJECTIVE,
                "He was *amazed* ... the view.", List.of("at", "by"), List.of("by", "of", "to"), "rule");

        assertInvalid(List.of(exercise), "distractor is also an answer: by");
    }

    @Test
    void rejectsSentenceWithoutGap() {
        assertInvalid(List.of(exercise("I'm *good* at chess.", "at")), "exactly one gap");
    }

    @Test
    void rejectsUnbalancedBold() {
        assertInvalid(List.of(exercise("I'm *good ... chess.", "at")), "unbalanced *");
    }

    @Test
    void rejectsMarkdownCharacters() {
        assertInvalid(List.of(exercise("I'm good ... snake_case.", "at")), "Markdown character _");
    }

    @Test
    void rejectsDuplicateSentence() {
        assertInvalid(List.of(exercise("I'm *good* ... chess.", "at"), exercise("I'm *good* ... chess.", "at")),
                "duplicate sentence");
    }

    @Test
    void rejectsTooFewDistractors() {
        PrepositionExercise exercise = new PrepositionExercise(PrepositionCategory.ADJECTIVE,
                "I'm *good* ... chess.", List.of("at"), List.of("in", "of"), "rule");

        assertInvalid(List.of(exercise), "less than 3 distractors");
    }

    @Test
    void rejectsTypoInYamlAnswer() {
        String yaml = """
                VERB:
                  - sentence: "Our plans *depend* ... the weather."
                    answers: ["onn"]
                    distractors: ["from", "of", "at"]
                    explanation: "depend on"
                """;

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> PrepositionCatalog.fromYaml(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))));
        assertTrue(error.getMessage().contains("unknown answer: onn"), error.getMessage());
    }

    static PrepositionCatalog loadRealCatalog() throws IOException {
        try (InputStream yaml = PrepositionCatalogTest.class.getResourceAsStream("/prepositions.yml")) {
            return PrepositionCatalog.fromYaml(yaml);
        }
    }

    private static PrepositionExercise exercise(String sentence, String answer) {
        return new PrepositionExercise(
                PrepositionCategory.ADJECTIVE, sentence, List.of(answer), List.of("in", "of", "on"), "rule");
    }

    private static void assertInvalid(List<PrepositionExercise> exercises, String expectedError) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new PrepositionCatalog(exercises));
        assertTrue(error.getMessage().contains(expectedError), error.getMessage());
    }
}
