package ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common;

import static ru.flashcards.telegram.bot.botapi.BotCommand.STOP_LEARNING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.AnswerKeyboardFactory;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;

/**
 * Builds the message of an exercise of a learning session.
 */
@Component
@RequiredArgsConstructor
public class ExerciseProvider {
    public static final String MEMORISED_ANSWER = "Запомнил";

    private static final int HIDDEN_LETTERS_PERCENT = 80;
    private static final int GAPS_WRONG_ANSWERS_QUANTITY = 3;
    private static final String GAP_PATTERN = "\\*([a-zA-Z]+)\\*";
    private static final String GAP_REPLACEMENT = "\\\\_\\\\_\\\\_\\\\_";

    private final FlashcardsDao flashcardsDao;
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final AnswerKeyboardFactory answerKeyboardFactory;
    private final Random random;

    public SendMessage newExercise(ExerciseFlashcard exercise) {
        return switch (exercise.exerciseKindsCode()) {
            case MEMORISED -> memorised(exercise);
            case CHECK_DESCRIPTION -> choice(exercise, "Выберите подходящее описание для ",
                    flashcardsDao.getRandomDescriptions(), exercise.description());
            case CHECK_TRANSLATION -> choice(exercise, "Выберите правильный перевод для ",
                    flashcardsDao.getRandomTranslations(), exercise.translation());
            case CHECK_SPELLING -> spelling(exercise, false);
            case CHECK_SPELLING_WITH_HELPS -> spelling(exercise, true);
            case COMPLETE_THE_GAPS -> completeTheGaps(exercise);
        };
    }

    private SendMessage memorised(ExerciseFlashcard exercise) {
        String text = "*" + exercise.word() + "* /" + exercise.transcription() + "/\n" + exercise.description()
                + "\n\n*Перевод:* " + exercise.translation();
        return message(exercise, text,
                answerKeyboardFactory.buttons(List.of(MEMORISED_ANSWER, STOP_LEARNING.command())));
    }

    private SendMessage choice(
            ExerciseFlashcard exercise, String task, List<String> wrongAnswers, String correctAnswer) {
        String text = task + "*" + exercise.word() + "* /" + exercise.transcription() + "/\n\n";
        return message(exercise, text, answerKeyboardFactory.answers(wrongAnswers, correctAnswer));
    }

    private SendMessage spelling(ExerciseFlashcard exercise, boolean withHint) {
        String text = String.format("Напишите на английском слово *%s*", exercise.translation());
        if (withHint) {
            text = text + "\n\nПодсказка: " + hideLetters(exercise.word()).replace("*", "\\*");
        }
        return message(exercise, text, answerKeyboardFactory.buttons(List.of(STOP_LEARNING.command())));
    }

    private SendMessage completeTheGaps(ExerciseFlashcard exercise) {
        String text = "Выберите корректное слово для заполнения пробела в предложении. \n\n"
                + exercise.example().replaceAll(GAP_PATTERN, GAP_REPLACEMENT);
        return message(exercise, text, answerKeyboardFactory.answers(gapsWrongAnswers(exercise), exercise.word()));
    }

    /**
     * Other words the user is learning make the exercise harder; random words are used when there are too few of them.
     */
    private List<String> gapsWrongAnswers(ExerciseFlashcard exercise) {
        List<String> unlearnedWords = new ArrayList<>(userProfileFlashcardsDao.findUnlearnedFlashcardKeyword(
                exercise.chatId(), GAPS_WRONG_ANSWERS_QUANTITY + 1));
        unlearnedWords.remove(exercise.word());
        if (unlearnedWords.size() < GAPS_WRONG_ANSWERS_QUANTITY) {
            return flashcardsDao.getRandomWords();
        }
        return unlearnedWords.subList(0, GAPS_WRONG_ANSWERS_QUANTITY);
    }

    /**
     * Hides {@value #HIDDEN_LETTERS_PERCENT}% of the letters, the first letter is always shown.
     */
    private String hideLetters(String word) {
        int hiddenLettersQuantity = word.length() * HIDDEN_LETTERS_PERCENT / 100;
        List<Integer> positions = IntStream.range(1, word.length()).boxed().collect(Collectors.toList());
        Collections.shuffle(positions, random);

        StringBuilder result = new StringBuilder(word);
        for (int position : positions.subList(0, Math.min(hiddenLettersQuantity, positions.size()))) {
            result.setCharAt(position, '*');
        }
        return result.toString();
    }

    private SendMessage message(ExerciseFlashcard exercise, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = BotMessages.markdown(exercise.chatId(), text);
        message.setReplyMarkup(keyboard);
        return message;
    }
}
