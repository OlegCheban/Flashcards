package ru.flashcards.telegram.bot.services.pronunciation;

import java.util.Optional;

/**
 * A source of pronunciation audio. Sources are tried in their {@link org.springframework.core.annotation.Order}
 * until one of them returns the audio.
 */
public interface PronunciationSource {

    /**
     * @return the pronunciation as MP3, empty if the source has no recording of the word
     * @throws ru.flashcards.telegram.bot.exception.PronunciationUnavailableException if the source fails
     */
    Optional<byte[]> findMp3(String word);
}
