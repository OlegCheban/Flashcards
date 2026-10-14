package ru.flashcards.telegram.bot.services.pronunciation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;

class WiktionaryPronunciationSourceTest {
    private static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0};

    private LocalHttpServer server;
    private WiktionaryPronunciationSource source;

    @BeforeEach
    void setUp() throws IOException {
        server = new LocalHttpServer();
        PronunciationHttpClient httpClient =
                new PronunciationHttpClient(HttpClient.newHttpClient(), new ObjectMapper());
        source = new WiktionaryPronunciationSource(httpClient, server.url("/wiktionary"), server.url("/commons"));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void downloadsMp3VersionOfAmericanRecording() {
        server.respond("/wiktionary", """
                {"query":{"pages":[{"title":"hello","images":[
                  {"title":"File:En-uk-hello.ogg"},
                  {"title":"File:LL-Q150 (fra)-LoquaxFR-hello.wav"},
                  {"title":"File:Wikipedia-logo-v2.svg"},
                  {"title":"File:en-us-hello.ogg"}]}]}}""");
        server.respond("/commons", """
                {"query":{"pages":[
                  {"title":"File:En-uk-hello.ogg","videoinfo":[{"derivatives":[
                    {"src":"%s","type":"audio/mpeg","transcodekey":"mp3"}]}]},
                  {"title":"File:En-us-hello.ogg","videoinfo":[{"derivatives":[
                    {"src":"https://example.org/original.ogg","type":"audio/ogg; codecs=\\"vorbis\\""},
                    {"src":"%s","type":"audio/mpeg","transcodekey":"mp3"}]}]}]}}"""
                .formatted(server.url("/uk.mp3"), server.url("/us.mp3")));
        server.respond("/us.mp3", 200, MP3);

        Optional<byte[]> mp3 = source.findMp3("hello");

        assertArrayEquals(MP3, mp3.orElseThrow());
        String commonsQuery = URLDecoder.decode(server.requests().get(1).getRawQuery(), StandardCharsets.UTF_8);
        assertTrue(commonsQuery.contains("titles=File:en-us-hello.ogg|File:En-uk-hello.ogg"), commonsQuery);
        assertTrue(server.userAgents().stream().allMatch(PronunciationHttpClient.USER_AGENT::equals));
    }

    @Test
    void usesLinguaLibreRecording() {
        server.respond("/wiktionary", """
                {"query":{"pages":[{"title":"intrinsic","images":[
                  {"title":"File:LL-Q1860 (eng)-Vealhurl-intrinsic.wav"}]}]}}""");
        server.respond("/commons", """
                {"query":{"pages":[{"title":"File:LL-Q1860 (eng)-Vealhurl-intrinsic.wav","videoinfo":[{"derivatives":[
                  {"src":"%s","type":"audio/mpeg","transcodekey":"mp3"}]}]}]}}""".formatted(server.url("/ll.mp3")));
        server.respond("/ll.mp3", 200, MP3);

        assertArrayEquals(MP3, source.findMp3("intrinsic").orElseThrow());
    }

    @Test
    void returnsEmptyWithoutEnglishRecording() {
        server.respond("/wiktionary", """
                {"query":{"pages":[{"title":"bonjour","images":[{"title":"File:Fr-bonjour.ogg"}]}]}}""");

        assertTrue(source.findMp3("bonjour").isEmpty());
        assertEquals(1, server.requests().size());
    }

    @Test
    void returnsEmptyForMissingPage() {
        server.respond("/wiktionary", """
                {"query":{"pages":[{"title":"qwertyuiop","missing":true}]}}""");

        assertTrue(source.findMp3("qwertyuiop").isEmpty());
    }

    @Test
    void rejectsDownloadThatIsNotMp3() {
        server.respond("/wiktionary", """
                {"query":{"pages":[{"title":"hello","images":[{"title":"File:en-us-hello.ogg"}]}]}}""");
        server.respond("/commons", """
                {"query":{"pages":[{"title":"File:En-us-hello.ogg","videoinfo":[{"derivatives":[
                  {"src":"%s","type":"audio/mpeg","transcodekey":"mp3"}]}]}]}}""".formatted(server.url("/page")));
        server.respond("/page", 200, "<html>".getBytes(StandardCharsets.UTF_8));

        assertThrows(PronunciationUnavailableException.class, () -> source.findMp3("hello"));
    }

    @Test
    void reportsUnavailableApi() {
        server.respond("/wiktionary", 503, new byte[0]);

        assertThrows(PronunciationUnavailableException.class, () -> source.findMp3("hello"));
    }
}
