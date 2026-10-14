package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

/**
 * HTTP calls of the dictionary sources. Wikimedia rejects requests without a descriptive User-Agent.
 */
@Component
@RequiredArgsConstructor
public class PronunciationHttpClient {
    static final String USER_AGENT = "FlashcardsBot/2.2 (https://github.com/OlegCheban/Flashcards)";

    /**
     * Pronunciation recordings take kilobytes; a bigger file is not a recording of a word.
     */
    private static final int MAX_AUDIO_BYTES = 2 * 1024 * 1024;
    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 404;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * @return empty for HTTP 404
     */
    public Optional<JsonNode> getJson(URI uri, Duration timeout) {
        HttpResponse<byte[]> response = get(uri, timeout);
        if (response.statusCode() == HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        requireOk(uri, response);
        try {
            return Optional.of(objectMapper.readTree(response.body()));
        } catch (IOException e) {
            throw new PronunciationUnavailableException("Invalid JSON: uri=" + uri, e);
        }
    }

    public byte[] getMp3(URI uri, Duration timeout) {
        HttpResponse<byte[]> response = get(uri, timeout);
        requireOk(uri, response);
        byte[] audio = response.body();
        if (audio.length > MAX_AUDIO_BYTES || !isMp3(audio)) {
            throw new PronunciationUnavailableException("Not an MP3 recording: uri=" + uri + ", bytes=" + audio.length);
        }
        return audio;
    }

    /**
     * An MP3 starts with an ID3 tag or with a frame sync (11 set bits).
     */
    static boolean isMp3(byte[] audio) {
        boolean hasId3 = audio.length > 3 && audio[0] == 'I' && audio[1] == 'D' && audio[2] == '3';
        boolean hasFrameSync = audio.length > 2 && (audio[0] & 0xFF) == 0xFF && (audio[1] & 0xE0) == 0xE0;
        return hasId3 || hasFrameSync;
    }

    private HttpResponse<byte[]> get(URI uri, Duration timeout) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PronunciationUnavailableException("Request interrupted: uri=" + uri, e);
        } catch (IOException e) {
            throw new PronunciationUnavailableException("Request failed: uri=" + uri, e);
        }
    }

    private static void requireOk(URI uri, HttpResponse<byte[]> response) {
        if (response.statusCode() != HTTP_OK) {
            throw new PronunciationUnavailableException(
                    "Unexpected response: uri=" + uri + ", status=" + response.statusCode());
        }
    }
}
