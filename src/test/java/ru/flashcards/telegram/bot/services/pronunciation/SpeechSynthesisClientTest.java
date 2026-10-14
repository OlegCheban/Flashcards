package ru.flashcards.telegram.bot.services.pronunciation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import ru.flashcards.telegram.bot.config.SpeechSynthesisProperties;
import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

class SpeechSynthesisClientTest {
    private static final byte[] WAV = {'R', 'I', 'F', 'F', 1, 2, 3};

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private HttpServer server;
    private int responseStatus;
    private byte[] responseBody;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/openai/v1/audio/speech", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(responseStatus, responseBody.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(responseBody);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void requestsWavSpeech() throws IOException {
        respond(200, WAV);

        byte[] wav = client().synthesize("intrinsic");

        assertArrayEquals(WAV, wav);
        assertEquals("Bearer test-key", authorization.get());
        JsonNode body = objectMapper.readTree(requestBody.get());
        assertEquals("canopylabs/orpheus-v1-english", body.get("model").asText());
        assertEquals("austin", body.get("voice").asText());
        assertEquals("intrinsic", body.get("input").asText());
        assertEquals("wav", body.get("response_format").asText());
    }

    @Test
    void removesVocalDirections() throws IOException {
        respond(200, WAV);

        client().synthesize("[cheerful] word");

        assertEquals("cheerful word", objectMapper.readTree(requestBody.get()).get("input").asText());
    }

    @Test
    void reportsApiError() {
        respond(400, "{\"error\":{\"code\":\"model_terms_required\"}}".getBytes(StandardCharsets.UTF_8));

        PronunciationUnavailableException error =
                assertThrows(PronunciationUnavailableException.class, () -> client().synthesize("word"));

        assertTrue(error.getMessage().contains("status=400"), error.getMessage());
        assertTrue(error.getMessage().contains("model_terms_required"), error.getMessage());
    }

    @Test
    void rejectsTooLongTextWithoutCallingTheApi() {
        String text = "a".repeat(SpeechSynthesisClient.MAX_INPUT_LENGTH + 1);

        assertThrows(PronunciationUnavailableException.class, () -> client().synthesize(text));
        assertNull(requestBody.get());
    }

    private void respond(int status, byte[] body) {
        responseStatus = status;
        responseBody = body;
    }

    private SpeechSynthesisClient client() {
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/openai/v1/audio/speech";
        SpeechSynthesisProperties properties =
                new SpeechSynthesisProperties(url, "test-key", "canopylabs/orpheus-v1-english", "austin");
        return new SpeechSynthesisClient(HttpClient.newHttpClient(), objectMapper, properties);
    }
}
