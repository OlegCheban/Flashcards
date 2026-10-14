package ru.flashcards.telegram.bot.services.pronunciation;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Free Dictionary API (https://dictionaryapi.dev): real recordings taken from Wiktionary.
 * The service is often unavailable, so the timeout is short.
 */
@Component
@Order(2)
public class DictionaryApiPronunciationSource implements PronunciationSource {
    private static final String ENTRIES_API = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final String AMERICAN_RECORDING_SUFFIX = "-us.mp3";

    private final PronunciationHttpClient httpClient;
    private final String entriesApi;

    @Autowired
    public DictionaryApiPronunciationSource(PronunciationHttpClient httpClient) {
        this(httpClient, ENTRIES_API);
    }

    DictionaryApiPronunciationSource(PronunciationHttpClient httpClient, String entriesApi) {
        this.httpClient = httpClient;
        this.entriesApi = entriesApi;
    }

    @Override
    public Optional<byte[]> findMp3(String word) {
        String encodedWord = URLEncoder.encode(word.trim(), StandardCharsets.UTF_8).replace("+", "%20");
        return httpClient.getJson(URI.create(entriesApi + encodedWord), TIMEOUT)
                .flatMap(DictionaryApiPronunciationSource::selectAudioUrl)
                .map(url -> httpClient.getMp3(URI.create(url), TIMEOUT));
    }

    /**
     * @return the American recording if present, otherwise the first available one
     */
    private static Optional<String> selectAudioUrl(JsonNode entries) {
        String firstAvailable = null;
        for (JsonNode entry : entries) {
            for (JsonNode phonetic : entry.path("phonetics")) {
                String audio = phonetic.path("audio").asText("");
                if (audio.isBlank()) {
                    continue;
                }
                String url = audio.startsWith("//") ? "https:" + audio : audio;
                if (url.contains(AMERICAN_RECORDING_SUFFIX)) {
                    return Optional.of(url);
                }
                if (firstAvailable == null) {
                    firstAvailable = url;
                }
            }
        }
        return Optional.ofNullable(firstAvailable);
    }
}
