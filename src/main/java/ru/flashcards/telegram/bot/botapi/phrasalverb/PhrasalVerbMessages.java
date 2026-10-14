package ru.flashcards.telegram.bot.botapi.phrasalverb;

import static ru.flashcards.telegram.bot.botapi.training.TrainingTexts.CORRECT_MARK;
import static ru.flashcards.telegram.bot.botapi.training.TrainingTexts.HINT_MARK;
import static ru.flashcards.telegram.bot.botapi.training.TrainingTexts.WRONG_MARK;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.AnswerKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.training.TrainingSession;
import ru.flashcards.telegram.bot.botapi.training.TrainingTexts;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

/**
 * Messages of the phrasal verb training. Definitions and examples are escaped for Markdown.
 */
@Component
@RequiredArgsConstructor
public class PhrasalVerbMessages {
    private final AnswerKeyboardFactory answerKeyboardFactory;

    public SendMessage start(Long chatId, int questionsQuantity) {
        return BotMessages.text(chatId, "Тренировка фразовых глаголов. Вопросов: " + questionsQuantity
                + ".\nОтвечайте номером варианта. Ошибки повторятся через пару вопросов. "
                + "Закончить досрочно — кнопка «Выйти».");
    }

    public SendMessage question(Long chatId, TrainingSession<PhrasalVerbQuestion> session) {
        PhrasalVerbQuestion question = session.currentQuestion()
                .orElseThrow(() -> new IllegalStateException("No current question: chatId=" + chatId));

        StringBuilder text = new StringBuilder(TrainingTexts.questionHeader(session))
                .append("\n\nВыберите подходящее значение для *").append(question.verb().verb()).append("*:\n");
        List<String> optionNumbers = new ArrayList<>();
        for (int i = 0; i < question.options().size(); i++) {
            String number = String.valueOf(i + 1);
            optionNumbers.add(number);
            text.append('\n').append(number).append(". ")
                    .append(BotMessages.escapeMarkdown(question.options().get(i)));
        }

        SendMessage message = BotMessages.markdown(chatId, text.toString());
        message.setReplyMarkup(answerKeyboardFactory.optionsRow(optionNumbers));
        return message;
    }

    public SendMessage feedback(Long chatId, TrainingSession.AnswerResult<PhrasalVerbQuestion> result) {
        PhrasalVerbQuestion question = result.question();
        PhrasalVerbMeaning meaning = question.meaning();

        StringBuilder text = new StringBuilder();
        if (result.isCorrect()) {
            text.append(CORRECT_MARK).append(' ').append(RandomMessageText.getPositiveMessage());
        } else {
            text.append(WRONG_MARK).append(" Правильный ответ: ").append(question.correctOptionNumber()).append('.');
        }
        text.append("\n*").append(question.verb().verb()).append("* — ")
                .append(BotMessages.escapeMarkdown(meaning.definition()))
                .append("\nПример: ").append(BotMessages.escapeMarkdown(meaning.example()));
        appendOtherMeanings(text, question);

        return BotMessages.markdown(chatId, text.toString());
    }

    public SendMessage summary(Long chatId, TrainingSession.Summary<PhrasalVerbQuestion> summary) {
        StringBuilder text = new StringBuilder(TrainingTexts.score(summary));
        if (summary.mistakes().isEmpty()) {
            text.append("\n\nБез ошибок, отлично!");
        } else {
            text.append("\n\nПовторите:");
            for (PhrasalVerbQuestion mistake : summary.mistakes()) {
                text.append("\n- *").append(mistake.verb().verb()).append("* — ")
                        .append(BotMessages.escapeMarkdown(mistake.meaning().definition()));
            }
        }
        text.append("\n\nЕщё раз: /pv");

        return BotMessages.withoutKeyboard(BotMessages.markdown(chatId, text.toString()));
    }

    public SendMessage sessionExpired(Long chatId) {
        return BotMessages.withoutKeyboard(
                BotMessages.text(chatId, "Тренировка не найдена или истекла. Начните заново: /pv"));
    }

    private void appendOtherMeanings(StringBuilder text, PhrasalVerbQuestion question) {
        List<PhrasalVerbMeaning> otherMeanings = new ArrayList<>(question.verb().meanings());
        otherMeanings.remove(question.meaning());
        if (otherMeanings.isEmpty()) {
            return;
        }

        text.append("\n\n").append(HINT_MARK).append(" Другие значения *").append(question.verb().verb()).append("*:");
        for (PhrasalVerbMeaning meaning : otherMeanings) {
            text.append("\n- ").append(BotMessages.escapeMarkdown(meaning.definition()));
        }
    }
}
