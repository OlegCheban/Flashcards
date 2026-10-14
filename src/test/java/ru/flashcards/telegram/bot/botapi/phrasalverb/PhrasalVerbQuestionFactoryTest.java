package ru.flashcards.telegram.bot.botapi.phrasalverb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PhrasalVerbQuestionFactoryTest {
    private static final int ATTEMPTS_PER_VERB = 50;

    private PhrasalVerbCatalog catalog;
    private PhrasalVerbQuestionFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        catalog = PhrasalVerbCatalogTest.loadRealCatalog();
        factory = new PhrasalVerbQuestionFactory(catalog, new Random(7));
    }

    @Test
    void offersFourDifferentOptionsWithOneCorrect() {
        for (PhrasalVerb verb : catalog.all()) {
            for (int i = 0; i < ATTEMPTS_PER_VERB; i++) {
                PhrasalVerbQuestion question = factory.createQuestion(verb);

                assertEquals(PhrasalVerbQuestionFactory.OPTIONS_QUANTITY, new HashSet<>(question.options()).size());
                assertTrue(verb.meanings().contains(question.meaning()));
                assertEquals(question.meaning().definition(),
                        question.options().get(question.correctOptionNumber() - 1));
            }
        }
    }

    @Test
    void neverOffersAnotherMeaningOfTheVerbOrASynonymAsWrongOption() {
        Map<String, Set<String>> sensesByDefinition = new HashMap<>();
        for (PhrasalVerb verb : catalog.all()) {
            for (PhrasalVerbMeaning meaning : verb.meanings()) {
                sensesByDefinition.put(meaning.definition(), new HashSet<>(meaning.senses()));
            }
        }

        for (PhrasalVerb verb : catalog.all()) {
            for (int i = 0; i < ATTEMPTS_PER_VERB; i++) {
                PhrasalVerbQuestion question = factory.createQuestion(verb);
                for (String option : question.options()) {
                    if (option.equals(question.meaning().definition())) {
                        continue;
                    }
                    assertTrue(Collections.disjoint(sensesByDefinition.get(option), verb.senses()),
                            verb.verb() + " offered as wrong option: " + option);
                }
            }
        }
    }

    @Test
    void asksEveryMeaningOfAVerb() {
        PhrasalVerb workOut = catalog.all().stream()
                .filter(verb -> verb.verb().equals("work out"))
                .findFirst()
                .orElseThrow();

        Set<PhrasalVerbMeaning> askedMeanings = new HashSet<>();
        for (int i = 0; i < ATTEMPTS_PER_VERB * 4; i++) {
            askedMeanings.add(factory.createQuestion(workOut).meaning());
        }

        assertEquals(new HashSet<>(workOut.meanings()), askedMeanings);
    }

    @Test
    void createsQuestionsForDifferentVerbs() {
        List<PhrasalVerbQuestion> questions = factory.createQuestions(PhrasalVerbTrainingMode.SESSION_SIZE);

        Set<String> verbs = new HashSet<>();
        for (PhrasalVerbQuestion question : questions) {
            verbs.add(question.verb().verb());
        }
        assertEquals(PhrasalVerbTrainingMode.SESSION_SIZE, verbs.size());
    }

    @Test
    void acceptsOptionNumberOrDefinition() {
        PhrasalVerbQuestion question = factory.createQuestion(catalog.all().get(0));
        int wrongNumber = question.correctOptionNumber() % PhrasalVerbQuestionFactory.OPTIONS_QUANTITY + 1;

        assertTrue(question.isCorrect(" " + question.correctOptionNumber() + " "));
        assertTrue(question.isCorrect(question.meaning().definition().toUpperCase()));
        assertFalse(question.isCorrect(String.valueOf(wrongNumber)));
    }
}
