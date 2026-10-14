package ru.flashcards.telegram.bot.botapi.preposition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * All preposition exercises. The data is validated on creation, so invalid content fails the application start.
 */
public class PrepositionCatalog {
    private final List<PrepositionExercise> exercises;

    public PrepositionCatalog(List<PrepositionExercise> exercises) {
        PrepositionExerciseValidator.validate(exercises);
        this.exercises = List.copyOf(exercises);
    }

    /**
     * Reads exercises grouped by category:
     * <pre>
     * VERB:
     *   - sentence: "Our success will *depend* ... the weather."
     *     answers: ["on", "upon"]
     *     distractors: ["from", "of", "at"]
     *     explanation: "depend on — зависеть от"
     * </pre>
     */
    public static PrepositionCatalog fromYaml(InputStream yaml) throws IOException {
        Map<PrepositionCategory, List<ExerciseData>> exercisesByCategory = new ObjectMapper(new YAMLFactory())
                .readValue(yaml, new TypeReference<LinkedHashMap<PrepositionCategory, List<ExerciseData>>>() { });

        List<PrepositionExercise> exercises = new ArrayList<>();
        for (Map.Entry<PrepositionCategory, List<ExerciseData>> category : exercisesByCategory.entrySet()) {
            for (ExerciseData data : category.getValue()) {
                exercises.add(new PrepositionExercise(
                        category.getKey(), data.sentence(), data.answers(), data.distractors(), data.explanation()));
            }
        }
        return new PrepositionCatalog(exercises);
    }

    public List<PrepositionExercise> all() {
        return exercises;
    }

    public List<PrepositionExercise> byCategory(PrepositionCategory category) {
        List<PrepositionExercise> result = new ArrayList<>();
        for (PrepositionExercise exercise : exercises) {
            if (exercise.category() == category) {
                result.add(exercise);
            }
        }
        return List.copyOf(result);
    }

    private record ExerciseData(String sentence, List<String> answers, List<String> distractors, String explanation) {
    }
}
