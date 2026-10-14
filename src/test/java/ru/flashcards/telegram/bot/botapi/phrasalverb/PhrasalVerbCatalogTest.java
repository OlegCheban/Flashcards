package ru.flashcards.telegram.bot.botapi.phrasalverb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class PhrasalVerbCatalogTest {
    /**
     * The training must contain exactly the phrasal verbs from the learning list.
     */
    private static final Set<String> LEARNING_LIST = Set.of(
            "mix up", "turn down", "come across", "fix up", "come up", "turn up", "run into", "fill in / fill out",
            "call off", "throw out", "do without", "do away with", "carry out", "turn out", "end up", "sort out",
            "come up with", "make out", "make for", "make of", "make off", "make up for", "make up",
            "bring up", "bring about", "bring out", "bring forward", "bring back", "bring down", "bring together",
            "bring in", "bring on", "bring off", "bring round",
            "take over", "take off", "take after", "take back", "take in", "take on", "take to", "take up",
            "get in", "get on", "get away with", "get into", "get over", "get through", "get behind with",
            "get down", "get down to", "get out of", "get across", "get around", "get round to", "get up to",
            "get off", "set aside", "set off", "set out", "set up",
            "put forward", "put in", "put across", "put down", "put together", "put up with", "put off", "put on",
            "put away", "put out", "put up", "keep up with", "drop off", "look into", "give in", "go over",
            "fall out", "show off", "build up", "come up against", "wear off", "work out", "wind down");

    @Test
    void containsExactlyTheLearningList() throws IOException {
        Set<String> verbs = new TreeSet<>();
        for (PhrasalVerb verb : loadRealCatalog().all()) {
            verbs.add(verb.verb());
        }

        assertEquals(new TreeSet<>(LEARNING_LIST), verbs);
    }

    @Test
    void rejectsDuplicateDefinition() {
        List<PhrasalVerb> verbs = List.of(
                verb("give in", "to stop resisting", "surrender"),
                verb("give up", "to stop resisting", "quit"));

        assertInvalid(verbs, "duplicate definition: to stop resisting");
    }

    @Test
    void rejectsMeaningWithoutSenses() {
        PhrasalVerb verb = new PhrasalVerb("give in",
                List.of(new PhrasalVerbMeaning("to stop resisting", List.of(), "He gave in.")));

        assertInvalid(List.of(verb), "no senses");
    }

    @Test
    void rejectsVerbWithoutEnoughWrongOptions() {
        List<PhrasalVerb> verbs = List.of(
                verb("give in", "to stop resisting", "surrender"),
                verb("set off", "to begin a journey", "depart"),
                verb("take off", "to leave the ground", "depart"));

        assertInvalid(verbs, "less than 3 possible wrong options");
    }

    static PhrasalVerbCatalog loadRealCatalog() throws IOException {
        try (InputStream yaml = PhrasalVerbCatalogTest.class.getResourceAsStream("/phrasal-verbs.yml")) {
            return PhrasalVerbCatalog.fromYaml(yaml);
        }
    }

    private static PhrasalVerb verb(String verb, String definition, String sense) {
        return new PhrasalVerb(verb, List.of(new PhrasalVerbMeaning(definition, List.of(sense), "Example.")));
    }

    private static void assertInvalid(List<PhrasalVerb> verbs, String expectedError) {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> new PhrasalVerbCatalog(verbs));
        assertTrue(error.getMessage().contains(expectedError), error.getMessage());
    }
}
