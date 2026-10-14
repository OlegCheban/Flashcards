package ru.flashcards.telegram.bot.services.pronunciation;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * Real recordings from Wiktionary: the word page lists audio files hosted on Wikimedia Commons,
 * Commons provides an MP3 version of every recording (Telegram does not play the original OGG).
 * Free and without an API key.
 */
@Component
@Order(1)
public class WiktionaryPronunciationSource implements PronunciationSource {
    private static final String WIKTIONARY_API = "https://en.wiktionary.org/w/api.php";
    private static final String COMMONS_API = "https://commons.wikimedia.org/w/api.php";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_CANDIDATES = 5;
    private static final Pattern AUDIO_FILE = Pattern.compile("(?i).+\\.(ogg|oga|opus|wav|flac|mp3)$");

    /**
     * English recordings, the most preferred first: American, Lingua Libre (Q1860 is English), British, any other.
     */
    private static final List<Pattern> PREFERRED_RECORDINGS = List.of(
            Pattern.compile("(?i)^File:en-us-.+"),
            Pattern.compile("^File:LL-Q1860 \\(eng\\)-.+"),
            Pattern.compile("(?i)^File:en-(uk|gb)-.+"),
            Pattern.compile("(?i)^File:en-.+"));

    private final PronunciationHttpClient httpClient;
    private final String wiktionaryApi;
    private final String commonsApi;

    @Autowired
    public WiktionaryPronunciationSource(PronunciationHttpClient httpClient) {
        this(httpClient, WIKTIONARY_API, COMMONS_API);
    }

    WiktionaryPronunciationSource(PronunciationHttpClient httpClient, String wiktionaryApi, String commonsApi) {
        this.httpClient = httpClient;
        this.wiktionaryApi = wiktionaryApi;
        this.commonsApi = commonsApi;
    }

    @Override
    public Optional<byte[]> findMp3(String word) {
        List<String> recordings = findRecordings(word);
        if (recordings.isEmpty()) {
            return Optional.empty();
        }
        return findMp3Url(recordings).map(url -> httpClient.getMp3(URI.create(url), TIMEOUT));
    }

    /**
     * @return English audio files of the word page, the most preferred first
     */
    private List<String> findRecordings(String word) {
        JsonNode pages = queryPages(wiktionaryApi
                + "?action=query&format=json&formatversion=2&redirects=1&prop=images&imlimit=50&titles="
                + encode(word.trim()));

        List<String> recordings = new ArrayList<>();
        for (JsonNode page : pages) {
            for (JsonNode image : page.path("images")) {
                String title = image.path("title").asText();
                if (AUDIO_FILE.matcher(title).matches() && preference(title) < PREFERRED_RECORDINGS.size()) {
                    recordings.add(title);
                }
            }
        }
        recordings.sort(Comparator.comparingInt(WiktionaryPronunciationSource::preference));
        return recordings.subList(0, Math.min(MAX_CANDIDATES, recordings.size()));
    }

    /**
     * @return the MP3 version of the most preferred recording that has one
     */
    private Optional<String> findMp3Url(List<String> recordings) {
        JsonNode pages = queryPages(commonsApi
                + "?action=query&format=json&formatversion=2&prop=videoinfo&viprop=derivatives&titles="
                + encode(String.join("|", recordings)));

        List<JsonNode> files = new ArrayList<>();
        pages.forEach(files::add);
        files.sort(Comparator.comparingInt(file -> preference(file.path("title").asText())));

        for (JsonNode file : files) {
            for (JsonNode derivative : file.path("videoinfo").path(0).path("derivatives")) {
                if (derivative.path("type").asText().startsWith("audio/mpeg")) {
                    return Optional.of(derivative.path("src").asText());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return the pages of a MediaWiki query, an empty node if the API answers 404
     */
    private JsonNode queryPages(String url) {
        return httpClient.getJson(URI.create(url), TIMEOUT)
                .map(json -> json.path("query").path("pages"))
                .orElse(MissingNode.getInstance());
    }

    private static int preference(String fileTitle) {
        for (int i = 0; i < PREFERRED_RECORDINGS.size(); i++) {
            if (PREFERRED_RECORDINGS.get(i).matcher(fileTitle).matches()) {
                return i;
            }
        }
        return PREFERRED_RECORDINGS.size();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
