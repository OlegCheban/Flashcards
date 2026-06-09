package ru.flashcards.telegram.bot.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Resolves a pronunciation audio URL for an English word using the free
 * Dictionary API (https://dictionaryapi.dev). No API key or rate limit.
 * The entries endpoint returns a "phonetics" array whose items may carry an
 * "audio" mp3 URL of a real human recording; not every word has one.
 */
@Service
public class PronunciationService {

    private static final String ENTRIES_API = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    /**
     * @return the first non-empty pronunciation audio URL for the word, or empty if none exists.
     */
    public Optional<String> findAudioUrl(String word) {
        if (word == null || word.isBlank()) {
            return Optional.empty();
        }

        try {
            String encoded = URLEncoder.encode(word.trim(), StandardCharsets.UTF_8).replace("+", "%20");
            HttpURLConnection connection = (HttpURLConnection) new URL(ENTRIES_API + encoded).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                connection.disconnect();
                return Optional.empty();
            }

            String body = read(connection);
            connection.disconnect();

            JSONArray entries = new JSONArray(body);
            String firstAvailable = null;
            for (int i = 0; i < entries.length(); i++) {
                JSONArray phonetics = entries.getJSONObject(i).optJSONArray("phonetics");
                if (phonetics == null) {
                    continue;
                }
                for (int j = 0; j < phonetics.length(); j++) {
                    String audio = normalize(phonetics.getJSONObject(j).optString("audio", ""));
                    if (audio == null) {
                        continue;
                    }
                    // Prefer the American (-us) recording; remember the first of any locale as a fallback.
                    if (audio.contains("-us.mp3")) {
                        return Optional.of(audio);
                    }
                    if (firstAvailable == null) {
                        firstAvailable = audio;
                    }
                }
            }
            return Optional.ofNullable(firstAvailable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /** Returns null for a blank URL; otherwise upgrades a protocol-relative URL to https. */
    private String normalize(String audio) {
        if (audio == null || audio.isBlank()) {
            return null;
        }
        return audio.startsWith("//") ? "https:" + audio : audio;
    }

    private String read(HttpURLConnection connection) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
