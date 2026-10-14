package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Creates questions for random phrasal verbs: a random meaning of the verb is correct,
 * the other options are meanings of other verbs that are wrong for it.
 */
@Component
@RequiredArgsConstructor
public class PhrasalVerbQuestionFactory {
    static final int OPTIONS_QUANTITY = 4;

    private final PhrasalVerbCatalog phrasalVerbCatalog;
    private final Random random;

    /**
     * @return questions for different verbs
     */
    public List<PhrasalVerbQuestion> createQuestions(int quantity) {
        List<PhrasalVerb> verbs = new ArrayList<>(phrasalVerbCatalog.all());
        Collections.shuffle(verbs, random);

        List<PhrasalVerbQuestion> questions = new ArrayList<>();
        for (PhrasalVerb verb : verbs.subList(0, Math.min(quantity, verbs.size()))) {
            questions.add(createQuestion(verb));
        }
        return questions;
    }

    PhrasalVerbQuestion createQuestion(PhrasalVerb verb) {
        PhrasalVerbMeaning meaning = verb.meanings().get(random.nextInt(verb.meanings().size()));

        List<String> distractors = new ArrayList<>(phrasalVerbCatalog.distractors(verb));
        Collections.shuffle(distractors, random);
        List<String> options = new ArrayList<>(distractors.subList(0, OPTIONS_QUANTITY - 1));
        options.add(random.nextInt(options.size() + 1), meaning.definition());

        return new PhrasalVerbQuestion(verb, meaning, options);
    }
}
