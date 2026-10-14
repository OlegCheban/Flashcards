package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * All phrasal verbs of the training and, for every verb, the meanings that may be offered as wrong options.
 * The data is validated on creation, so invalid content fails the application start.
 */
public class PhrasalVerbCatalog {
    static final int MIN_DISTRACTORS = 3;

    private final List<PhrasalVerb> verbs;
    private final Map<String, List<String>> distractorsByVerb;

    public PhrasalVerbCatalog(List<PhrasalVerb> verbs) {
        PhrasalVerbValidator.validate(verbs);
        this.verbs = List.copyOf(verbs);
        this.distractorsByVerb = collectDistractors(this.verbs);
        validateDistractors();
    }

    /**
     * Reads a list of verbs:
     * <pre>
     * - verb: "turn down"
     *   meanings:
     *     - definition: "to refuse an offer or request"
     *       senses: ["refuse"]
     *       example: "She turned down the job offer."
     * </pre>
     */
    public static PhrasalVerbCatalog fromYaml(InputStream yaml) throws IOException {
        List<PhrasalVerb> verbs = new ObjectMapper(new YAMLFactory())
                .readValue(yaml, new TypeReference<List<PhrasalVerb>>() { });
        return new PhrasalVerbCatalog(verbs);
    }

    public List<PhrasalVerb> all() {
        return verbs;
    }

    /**
     * Definitions of other verbs that share no sense with any meaning of this verb,
     * so none of them is a correct answer for it.
     */
    public List<String> distractors(PhrasalVerb verb) {
        return distractorsByVerb.getOrDefault(verb.verb(), List.of());
    }

    private static Map<String, List<String>> collectDistractors(List<PhrasalVerb> verbs) {
        Map<String, List<String>> distractorsByVerb = new HashMap<>();
        for (PhrasalVerb verb : verbs) {
            Set<String> verbSenses = verb.senses();
            Set<String> distractors = new LinkedHashSet<>();
            for (PhrasalVerb other : verbs) {
                if (other.verb().equals(verb.verb())) {
                    continue;
                }
                for (PhrasalVerbMeaning meaning : other.meanings()) {
                    if (Collections.disjoint(meaning.senses(), verbSenses)) {
                        distractors.add(meaning.definition());
                    }
                }
            }
            distractorsByVerb.put(verb.verb(), List.copyOf(distractors));
        }
        return distractorsByVerb;
    }

    private void validateDistractors() {
        List<String> errors = new ArrayList<>();
        for (PhrasalVerb verb : verbs) {
            if (distractors(verb).size() < MIN_DISTRACTORS) {
                errors.add("\"" + verb.verb() + "\": less than " + MIN_DISTRACTORS + " possible wrong options");
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid phrasal verbs:\n" + String.join("\n", errors));
        }
    }
}
