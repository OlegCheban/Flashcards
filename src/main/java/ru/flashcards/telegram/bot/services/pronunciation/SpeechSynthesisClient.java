package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.config.SpeechSynthesisProperties;
import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

/**
 * Client of a text-to-speech API in the OpenAI {@code /audio/speech} format (Groq Orpheus).
 * Orpheus returns WAV only and accepts at most {@value #MAX_INPUT_LENGTH} characters.
 */
@Component
@RequiredArgsConstructor
public class SpeechSynthesisClient {
    static final int MAX_INPUT_LENGTH = 200;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String RESPONSE_FORMAT = "wav";
    private static final int HTTP_OK = 200;
    private static final int MAX_LOGGED_BODY_LENGTH = 300;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SpeechSynthesisProperties properties;

    /**
     * @return the speech as WAV
     * @throws PronunciationUnavailableException if the API is unavailable or rejects the request
     */
    public byte[] synthesize(String text) {
        HttpResponse<byte[]> response = send(buildRequest(sanitize(text)));
        if (response.statusCode() != HTTP_OK) {
            throw new PronunciationUnavailableException("Speech synthesis failed: status=" + response.statusCode()
                    + ", body=" + abbreviate(new String(response.body(), StandardCharsets.UTF_8)));
        }
        return response.body();
    }

    /**
     * Square brackets are Orpheus vocal directions such as [cheerful], they must not come from the content.
     */
    private String sanitize(String text) {
        String sanitized = text.replace("[", "").replace("]", "").trim();
        if (sanitized.isEmpty() || sanitized.length() > MAX_INPUT_LENGTH) {
            throw new PronunciationUnavailableException(
                    "Text for speech synthesis must have 1-" + MAX_INPUT_LENGTH + " characters: " + abbreviate(text));
        }
        return sanitized;
    }

    private HttpRequest buildRequest(String text) {
        Map<String, String> body = Map.of(
                "model", properties.model(),
                "voice", properties.voice(),
                "input", text,
                "response_format", RESPONSE_FORMAT);
        try {
            return HttpRequest.newBuilder(URI.create(properties.url()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
        } catch (JsonProcessingException e) {
            throw new PronunciationUnavailableException("Unable to build speech synthesis request", e);
        }
    }

    private HttpResponse<byte[]> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PronunciationUnavailableException("Speech synthesis request interrupted", e);
        } catch (IOException e) {
            throw new PronunciationUnavailableException("Speech synthesis request failed", e);
        }
    }

    private static String abbreviate(String text) {
        return text.length() <= MAX_LOGGED_BODY_LENGTH ? text : text.substring(0, MAX_LOGGED_BODY_LENGTH) + "...";
    }
}
