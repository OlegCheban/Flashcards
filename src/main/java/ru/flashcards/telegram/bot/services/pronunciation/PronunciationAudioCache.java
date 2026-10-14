package ru.flashcards.telegram.bot.services.pronunciation;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Telegram file_id of the pronunciation already uploaded for a word: the audio is sent again by file_id
 * without new speech synthesis. A file_id stays valid for this bot, so entries do not expire;
 * the cache is in memory and is filled again after a restart.
 */
@Component
public class PronunciationAudioCache {
    private static final long MAX_WORDS = 100_000;

    private final Cache<String, String> fileIds = Caffeine.newBuilder()
            .maximumSize(MAX_WORDS)
            .build();

    public Optional<String> find(String word) {
        return Optional.ofNullable(fileIds.getIfPresent(key(word)));
    }

    public void put(String word, String fileId) {
        fileIds.put(key(word), fileId);
    }

    public void remove(String word) {
        fileIds.invalidate(key(word));
    }

    private static String key(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }
}
