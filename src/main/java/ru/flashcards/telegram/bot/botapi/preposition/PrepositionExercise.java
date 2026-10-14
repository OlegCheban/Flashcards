package ru.flashcards.telegram.bot.botapi.preposition;

import java.util.List;
import java.util.Locale;

import ru.flashcards.telegram.bot.botapi.training.TrainingQuestion;

/**
 * A sentence with a gap ({@value #GAP}) to fill with a preposition.
 *
 * @param answers     every acceptable answer, the first one is shown on the keyboard
 * @param distractors wrong answers offered on the keyboard; none of them is acceptable in this sentence
 * @param explanation the rule in Russian, shown after a mistake
 */
public record PrepositionExercise(
        PrepositionCategory category,
        String sentence,
        List<String> answers,
        List<String> distractors,
        String explanation) implements TrainingQuestion {

    public static final String GAP = "...";

    public PrepositionExercise {
        answers = answers == null ? List.of() : List.copyOf(answers);
        distractors = distractors == null ? List.of() : List.copyOf(distractors);
    }

    @Override
    public boolean isCorrect(String answer) {
        return answers.contains(normalize(answer));
    }

    public String mainAnswer() {
        return answers.get(0);
    }

    /**
     * The sentence with the gap filled by the main answer in bold (Markdown).
     */
    public String sentenceWithAnswer() {
        return sentence.replace(GAP, "*" + mainAnswer() + "*");
    }

    static String normalize(String answer) {
        return answer.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
