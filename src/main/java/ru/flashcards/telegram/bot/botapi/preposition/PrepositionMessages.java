package ru.flashcards.telegram.bot.botapi.preposition;

import static ru.flashcards.telegram.bot.botapi.training.TrainingTexts.CORRECT_MARK;
import static ru.flashcards.telegram.bot.botapi.training.TrainingTexts.HINT_MARK;
import static ru.flashcards.telegram.bot.botapi.training.TrainingTexts.WRONG_MARK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.AnswerKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.training.TrainingSession;
import ru.flashcards.telegram.bot.botapi.training.TrainingTexts;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

/**
 * Messages of the preposition training. Sentences are validated to be safe Markdown, explanations are escaped.
 */
@Component
@RequiredArgsConstructor
public class PrepositionMessages {
    private static final int DISTRACTORS_QUANTITY = 3;

    private final AnswerKeyboardFactory answerKeyboardFactory;
    private final Random random;

    /**
     * @param category the topic, {@code null} for all topics
     */
    public SendMessage start(Long chatId, PrepositionCategory category, int questionsQuantity) {
        String topic = category == null ? "все темы" : category.title();
        return BotMessages.text(chatId, "Тренировка предлогов: " + topic + ". Вопросов: " + questionsQuantity
                + ".\nОшибки повторятся через пару вопросов. Закончить досрочно — кнопка «Выйти».");
    }

    public SendMessage question(Long chatId, TrainingSession<PrepositionExercise> session) {
        PrepositionExercise exercise = session.currentQuestion()
                .orElseThrow(() -> new IllegalStateException("No current exercise: chatId=" + chatId));

        SendMessage message = BotMessages.markdown(chatId,
                TrainingTexts.questionHeader(session) + "\n\n" + exercise.sentence());
        message.setReplyMarkup(answerKeyboardFactory.answers(pickDistractors(exercise), exercise.mainAnswer()));
        return message;
    }

    public SendMessage feedback(Long chatId, TrainingSession.AnswerResult<PrepositionExercise> result) {
        PrepositionExercise exercise = result.question();
        if (result.isCorrect()) {
            String text = CORRECT_MARK + " " + RandomMessageText.getPositiveMessage();
            if (exercise.answers().size() > 1) {
                text = text + "\nПодходят: " + boldAnswers(exercise);
            }
            return BotMessages.markdown(chatId, text);
        }

        return BotMessages.markdown(chatId, WRONG_MARK + " Правильный ответ: " + boldAnswers(exercise) + "\n"
                + exercise.sentenceWithAnswer() + "\n\n"
                + HINT_MARK + " " + BotMessages.escapeMarkdown(exercise.explanation()));
    }

    public SendMessage summary(Long chatId, TrainingSession.Summary<PrepositionExercise> summary) {
        StringBuilder text = new StringBuilder(TrainingTexts.score(summary));
        if (summary.mistakes().isEmpty()) {
            text.append("\n\nБез ошибок, отлично!");
        } else {
            text.append("\n\nПовторите:");
            for (PrepositionExercise mistake : summary.mistakes()) {
                text.append("\n- ").append(mistake.sentenceWithAnswer());
            }
        }
        text.append("\n\nЕщё раз: /pr, выбрать тему: /pr topics");

        return BotMessages.withoutKeyboard(BotMessages.markdown(chatId, text.toString()));
    }

    public SendMessage topics(Long chatId) {
        StringBuilder text = new StringBuilder("Темы тренировки предлогов:\n/pr — все темы");
        for (PrepositionCategory category : PrepositionCategory.values()) {
            text.append("\n/pr ").append(category.code()).append(" — ").append(category.title());
        }
        return BotMessages.text(chatId, text.toString());
    }

    public SendMessage sessionExpired(Long chatId) {
        return BotMessages.withoutKeyboard(
                BotMessages.text(chatId, "Тренировка не найдена или истекла. Начните заново: /pr"));
    }

    private List<String> pickDistractors(PrepositionExercise exercise) {
        List<String> distractors = new ArrayList<>(exercise.distractors());
        Collections.shuffle(distractors, random);
        return distractors.subList(0, Math.min(DISTRACTORS_QUANTITY, distractors.size()));
    }

    private String boldAnswers(PrepositionExercise exercise) {
        List<String> answers = new ArrayList<>();
        for (String answer : exercise.answers()) {
            answers.add("*" + answer + "*");
        }
        return String.join(" / ", answers);
    }
}
