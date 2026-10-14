package ru.flashcards.telegram.bot.services.pronunciation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.http.HttpClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

class DictionaryApiPronunciationSourceTest {
    private static final byte[] MP3 = {(byte) 0xFF, (byte) 0xFB, 0x50, 0};

    private LocalHttpServer server;
    private DictionaryApiPronunciationSource source;

    @BeforeEach
    void setUp() throws IOException {
        server = new LocalHttpServer();
        PronunciationHttpClient httpClient =
                new PronunciationHttpClient(HttpClient.newHttpClient(), new ObjectMapper());
        source = new DictionaryApiPronunciationSource(httpClient, server.url("/entries/"));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void prefersAmericanRecording() {
        server.respond("/entries/hello", """
                [{"word":"hello","phonetics":[
                  {"audio":"%s"},
                  {"text":"/həˈloʊ/","audio":""},
                  {"audio":"%s"}]}]""".formatted(server.url("/hello-uk.mp3"), server.url("/hello-us.mp3")));
        server.respond("/hello-us.mp3", 200, MP3);

        assertArrayEquals(MP3, source.findMp3("hello").orElseThrow());
    }

    @Test
    void returnsEmptyForUnknownWord() {
        assertTrue(source.findMp3("qwertyuiop").isEmpty());
    }

    @Test
    void returnsEmptyWithoutAudio() {
        server.respond("/entries/word", """
                [{"word":"word","phonetics":[{"text":"/wɜːd/","audio":""}]}]""");

        assertTrue(source.findMp3("word").isEmpty());
    }

    @Test
    void reportsUnavailableService() {
        server.respond("/entries/hello", 522, new byte[0]);

        assertThrows(PronunciationUnavailableException.class, () -> source.findMp3("hello"));
    }
}
