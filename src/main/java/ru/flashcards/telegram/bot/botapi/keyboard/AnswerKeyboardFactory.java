package ru.flashcards.telegram.bot.botapi.keyboard;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import lombok.RequiredArgsConstructor;

/**
 * Builds reply keyboards for quiz-like exercises: one button per row and an exit button at the bottom.
 */
@Component
@RequiredArgsConstructor
public class AnswerKeyboardFactory {
    private final Random random;

    /**
     * Places the correct answer at a random position among the wrong ones.
     * Wrong answers that duplicate the correct one are dropped, so the correct answer is always shown exactly once.
     */
    public ReplyKeyboardMarkup answers(List<String> wrongAnswers, String correctAnswer) {
        List<String> answers = new ArrayList<>();
        for (String wrongAnswer : wrongAnswers) {
            if (wrongAnswer != null && !wrongAnswer.equalsIgnoreCase(correctAnswer) && !answers.contains(wrongAnswer)) {
                answers.add(wrongAnswer);
            }
        }
        answers.add(random.nextInt(answers.size() + 1), correctAnswer);
        answers.add(STOP_LEARNING.command());

        return buttons(answers);
    }

    /**
     * Short option labels (e.g. numbers) in one row and the exit button below.
     */
    public ReplyKeyboardMarkup optionsRow(List<String> labels) {
        KeyboardRow optionsRow = new KeyboardRow();
        for (String label : labels) {
            optionsRow.add(new KeyboardButton(label));
        }
        KeyboardRow exitRow = new KeyboardRow();
        exitRow.add(new KeyboardButton(STOP_LEARNING.command()));

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(optionsRow, exitRow));
        markup.setResizeKeyboard(true);
        return markup;
    }

    public ReplyKeyboardMarkup buttons(List<String> labels) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (String label : labels) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(label));
            rows.add(row);
        }

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }
}
