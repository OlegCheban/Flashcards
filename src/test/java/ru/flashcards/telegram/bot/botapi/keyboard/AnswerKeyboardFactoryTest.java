package ru.flashcards.telegram.bot.botapi.keyboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

class AnswerKeyboardFactoryTest {
    private final AnswerKeyboardFactory factory = new AnswerKeyboardFactory(new Random());

    @RepeatedTest(20)
    void containsCorrectAnswerOnceWhateverTheNumberOfWrongAnswers() {
        for (int wrongAnswersQuantity = 0; wrongAnswersQuantity <= 4; wrongAnswersQuantity++) {
            List<String> wrongAnswers = new ArrayList<>();
            for (int i = 0; i < wrongAnswersQuantity; i++) {
                wrongAnswers.add("wrong" + i);
            }

            List<String> labels = labels(factory.answers(wrongAnswers, "correct"));

            assertEquals(1, labels.stream().filter("correct"::equals).count());
            assertEquals(wrongAnswersQuantity + 2, labels.size());
            assertEquals(STOP_LEARNING.command(), labels.get(labels.size() - 1));
        }
    }

    @Test
    void dropsWrongAnswersThatDuplicateTheCorrectOne() {
        List<String> labels = labels(factory.answers(List.of("Correct", "other", "other"), "correct"));

        assertEquals(List.of("correct", "other"), labels.subList(0, labels.size() - 1).stream().sorted().toList());
    }

    @Test
    void placesCorrectAnswerAtEveryPosition() {
        boolean[] positions = new boolean[4];
        for (int i = 0; i < 200; i++) {
            List<String> labels = labels(factory.answers(List.of("a", "b", "c"), "correct"));
            positions[labels.indexOf("correct")] = true;
        }

        for (boolean isUsed : positions) {
            assertEquals(true, isUsed);
        }
    }

    private List<String> labels(ReplyKeyboardMarkup markup) {
        List<String> labels = new ArrayList<>();
        for (KeyboardRow row : markup.getKeyboard()) {
            labels.add(row.get(0).getText());
        }
        return labels;
    }
}
