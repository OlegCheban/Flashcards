package ru.flashcards.telegram.bot.botapi.training;

import java.time.Duration;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Current training session of every user. Kept in memory: an abandoned session expires,
 * a restart loses all sessions (the user starts the training again).
 */
public class TrainingSessionStore<Q extends TrainingQuestion> {
    private static final long MAX_SESSIONS = 10_000;
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    private final Cache<Long, TrainingSession<Q>> sessions = Caffeine.newBuilder()
            .maximumSize(MAX_SESSIONS)
            .expireAfterAccess(SESSION_TTL)
            .build();

    public void start(Long chatId, TrainingSession<Q> session) {
        sessions.put(chatId, session);
    }

    public Optional<TrainingSession<Q>> find(Long chatId) {
        return Optional.ofNullable(sessions.getIfPresent(chatId));
    }

    public void finish(Long chatId) {
        sessions.invalidate(chatId);
    }
}
