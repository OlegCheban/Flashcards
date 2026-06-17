package ru.flashcards.telegram.bot.botapi.preposition;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class UserPrepositionMistakes {
    private static final long MAX_TRACKED_CHATS = 10_000;
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    private final Cache<Long, ConcurrentMap<String, Integer>> mistakes = Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_CHATS)
            .expireAfterWrite(SESSION_TTL)
            .build();

    public void putNewMistake(Long chatId, String sentence){
        var userMistakes = mistakes.get(chatId, id -> new ConcurrentHashMap<>());
        userMistakes.merge(sentence, 1, Integer::sum);
    }

    public Map<String, Integer> getMistakes(Long chatId) {
        var userMistakes = mistakes.getIfPresent(chatId);
        return userMistakes != null ? Map.copyOf(userMistakes) : Map.of();
    }

    public void clearMistakes(Long chatId) {
        mistakes.invalidate(chatId);
    }
}
