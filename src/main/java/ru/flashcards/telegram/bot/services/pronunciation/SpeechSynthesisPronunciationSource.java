package ru.flashcards.telegram.bot.services.pronunciation;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Synthetic speech: works for any word, so it is the last resort after the real recordings.
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class SpeechSynthesisPronunciationSource implements PronunciationSource {
    private final SpeechSynthesisClient speechSynthesisClient;
    private final Mp3Encoder mp3Encoder;

    @Override
    public Optional<byte[]> findMp3(String word) {
        return Optional.of(mp3Encoder.encode(speechSynthesisClient.synthesize(word)));
    }
}
