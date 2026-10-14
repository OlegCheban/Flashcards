package ru.flashcards.telegram.bot.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves a pronunciation audio URL for an English word using the free
 * Dictionary API (https://dictionaryapi.dev). No API key or rate limit.
 * The entries endpoint returns a "phonetics" array whose items may carry an
 * "audio" mp3 URL of a real human recording; not every word has one.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PronunciationService {

    private static final String ENTRIES_API = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final int HTTP_OK = 200;
    private static final String AMERICAN_RECORDING_SUFFIX = "-us.mp3";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * @return the American pronunciation audio URL if present, otherwise the first available one;
     *         empty if the word has no recording or the dictionary is unavailable
     */
    public Optional<String> findAudioUrl(String word) {
        if (word == null || word.isBlank()) {
            return Optional.empty();
        }

        try {
            HttpResponse<String> response = httpClient.send(buildRequest(word), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HTTP_OK) {
                log.debug("Pronunciation not found: word={}, status={}", word, response.statusCode());
                return Optional.empty();
            }
            return selectAudioUrl(objectMapper.readTree(response.body()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Pronunciation request interrupted: word={}", word);
        } catch (IOException e) {
            log.warn("Pronunciation request failed: word={}", word, e);
        }
        return Optional.empty();
    }

    private HttpRequest buildRequest(String word) {
        String encodedWord = URLEncoder.encode(word.trim(), StandardCharsets.UTF_8).replace("+", "%20");
        return HttpRequest.newBuilder(URI.create(ENTRIES_API + encodedWord))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
    }

    private Optional<String> selectAudioUrl(JsonNode entries) {
        String firstAvailable = null;
        for (JsonNode entry : entries) {
            for (JsonNode phonetic : entry.path("phonetics")) {
                String audio = normalize(phonetic.path("audio").asText(""));
                if (audio == null) {
                    continue;
                }
                if (audio.contains(AMERICAN_RECORDING_SUFFIX)) {
                    return Optional.of(audio);
                }
                if (firstAvailable == null) {
                    firstAvailable = audio;
                }
            }
        }
        return Optional.ofNullable(firstAvailable);
    }

    /**
     * Returns null for a blank URL; otherwise upgrades a protocol-relative URL to https.
     */
    private String normalize(String audio) {
        if (audio.isBlank()) {
            return null;
        }
        return audio.startsWith("//") ? "https:" + audio : audio;
    }
}
