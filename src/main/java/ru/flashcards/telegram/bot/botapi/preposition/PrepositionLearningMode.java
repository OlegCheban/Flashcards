package ru.flashcards.telegram.bot.botapi.preposition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.UserMode;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.training.TrainingSession;
import ru.flashcards.telegram.bot.botapi.training.TrainingSessionStore;

/**
 * The preposition training: {@code /pr [topic]} starts a session of {@value #SESSION_SIZE} random exercises,
 * every answer gets feedback, the end of the session or "Выйти" shows the result.
 */
@Component
@RequiredArgsConstructor
public class PrepositionLearningMode {
    public static final int SESSION_SIZE = 10;

    private final PrepositionCatalog prepositionCatalog;
    private final TrainingSessionStore<PrepositionExercise> prepositionSessionStore;
    private final PrepositionMessages prepositionMessages;
    private final UserModeSettings userModeSettings;
    private final Random random;

    /**
     * @param topic a {@link PrepositionCategory} code, empty for all topics; an unknown topic shows the topic list
     */
    public List<BotApiMethod<?>> start(Long chatId, String topic) {
        Optional<PrepositionCategory> category = Optional.empty();
        if (!topic.isBlank()) {
            category = PrepositionCategory.fromCode(topic);
            if (category.isEmpty()) {
                return List.of(prepositionMessages.topics(chatId));
            }
        }

        List<PrepositionExercise> exercises = category.map(prepositionCatalog::byCategory)
                .orElseGet(prepositionCatalog::all);
        TrainingSession<PrepositionExercise> session = new TrainingSession<>(pickRandom(exercises));
        prepositionSessionStore.start(chatId, session);
        userModeSettings.setMode(chatId, UserMode.PREPOSITION);

        return List.of(
                prepositionMessages.start(chatId, category.orElse(null), session.questionsQuantity()),
                prepositionMessages.question(chatId, session));
    }

    public List<BotApiMethod<?>> answer(Long chatId, String answer) {
        Optional<TrainingSession<PrepositionExercise>> session = prepositionSessionStore.find(chatId);
        if (session.isEmpty()) {
            userModeSettings.removeMode(chatId);
            return List.of(prepositionMessages.sessionExpired(chatId));
        }

        List<BotApiMethod<?>> replies = new ArrayList<>();
        session.get().answer(answer)
                .ifPresent(result -> replies.add(prepositionMessages.feedback(chatId, result)));
        if (session.get().isFinished()) {
            replies.add(finish(chatId, session.get()));
        } else {
            replies.add(prepositionMessages.question(chatId, session.get()));
        }
        return replies;
    }

    public List<BotApiMethod<?>> stop(Long chatId) {
        Optional<TrainingSession<PrepositionExercise>> session = prepositionSessionStore.find(chatId);
        if (session.isEmpty()) {
            userModeSettings.removeMode(chatId);
            return List.of(prepositionMessages.sessionExpired(chatId));
        }
        return List.of(finish(chatId, session.get()));
    }

    private BotApiMethod<?> finish(Long chatId, TrainingSession<PrepositionExercise> session) {
        prepositionSessionStore.finish(chatId);
        userModeSettings.removeMode(chatId);
        return prepositionMessages.summary(chatId, session.summary());
    }

    private List<PrepositionExercise> pickRandom(List<PrepositionExercise> exercises) {
        List<PrepositionExercise> shuffled = new ArrayList<>(exercises);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(SESSION_SIZE, shuffled.size()));
    }
}
