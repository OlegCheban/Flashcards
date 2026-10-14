package ru.flashcards.telegram.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Text-to-speech API in the OpenAI {@code /audio/speech} format (Groq).
 */
@ConfigurationProperties(prefix = "speech-synthesis")
public record SpeechSynthesisProperties(String url, String apiKey, String model, String voice) {
}
