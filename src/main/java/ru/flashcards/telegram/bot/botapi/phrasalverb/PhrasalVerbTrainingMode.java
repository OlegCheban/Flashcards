package ru.flashcards.telegram.bot.botapi.phrasalverb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.UserMode;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.training.TrainingSession;
import ru.flashcards.telegram.bot.botapi.training.TrainingSessionStore;

/**
 * The phrasal verb training: {@code /pv} starts a session of {@value #SESSION_SIZE} questions about different verbs,
 * every answer gets feedback, the end of the session or "Выйти" shows the result.
 */
@Component
@RequiredArgsConstructor
public class PhrasalVerbTrainingMode {
    public static final int SESSION_SIZE = 10;

    private final PhrasalVerbQuestionFactory phrasalVerbQuestionFactory;
    private final TrainingSessionStore<PhrasalVerbQuestion> phrasalVerbSessionStore;
    private final PhrasalVerbMessages phrasalVerbMessages;
    private final UserModeSettings userModeSettings;

    public List<BotApiMethod<?>> start(Long chatId) {
        TrainingSession<PhrasalVerbQuestion> session =
                new TrainingSession<>(phrasalVerbQuestionFactory.createQuestions(SESSION_SIZE));
        phrasalVerbSessionStore.start(chatId, session);
        userModeSettings.setMode(chatId, UserMode.PHRASAL_VERB);

        return List.of(
                phrasalVerbMessages.start(chatId, session.questionsQuantity()),
                phrasalVerbMessages.question(chatId, session));
    }

    public List<BotApiMethod<?>> answer(Long chatId, String answer) {
        Optional<TrainingSession<PhrasalVerbQuestion>> session = phrasalVerbSessionStore.find(chatId);
        if (session.isEmpty()) {
            userModeSettings.removeMode(chatId);
            return List.of(phrasalVerbMessages.sessionExpired(chatId));
        }

        List<BotApiMethod<?>> replies = new ArrayList<>();
        session.get().answer(answer)
                .ifPresent(result -> replies.add(phrasalVerbMessages.feedback(chatId, result)));
        if (session.get().isFinished()) {
            replies.add(finish(chatId, session.get()));
        } else {
            replies.add(phrasalVerbMessages.question(chatId, session.get()));
        }
        return replies;
    }

    public List<BotApiMethod<?>> stop(Long chatId) {
        Optional<TrainingSession<PhrasalVerbQuestion>> session = phrasalVerbSessionStore.find(chatId);
        if (session.isEmpty()) {
            userModeSettings.removeMode(chatId);
            return List.of(phrasalVerbMessages.sessionExpired(chatId));
        }
        return List.of(finish(chatId, session.get()));
    }

    private BotApiMethod<?> finish(Long chatId, TrainingSession<PhrasalVerbQuestion> session) {
        phrasalVerbSessionStore.finish(chatId);
        userModeSettings.removeMode(chatId);
        return phrasalVerbMessages.summary(chatId, session.summary());
    }
}
