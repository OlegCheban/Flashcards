package ru.flashcards.telegram.bot.botapi.training;

/**
 * Texts shared by all trainings.
 */
public final class TrainingTexts {
    public static final String CORRECT_MARK = "\u2705";
    public static final String WRONG_MARK = "\u274C";
    public static final String HINT_MARK = "\uD83D\uDCA1";

    private TrainingTexts() {
    }

    /**
     * "Вопрос 3/10" for a new question, "Повтор" for a question asked again after a mistake.
     */
    public static String questionHeader(TrainingSession<?> session) {
        return session.isCurrentRepeated()
                ? "Повтор"
                : "Вопрос " + session.currentQuestionNumber() + "/" + session.questionsQuantity();
    }

    public static String score(TrainingSession.Summary<?> summary) {
        StringBuilder text = new StringBuilder("Тренировка завершена.\nПравильно с первой попытки: ")
                .append(summary.correctFirstAttempts()).append(" из ").append(summary.answeredQuestions());
        if (summary.answeredQuestions() < summary.questionsQuantity()) {
            text.append(" (пройдено ").append(summary.answeredQuestions())
                    .append(" из ").append(summary.questionsQuantity()).append(')');
        }
        return text.toString();
    }
}
