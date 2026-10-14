package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpServer;

/**
 * An HTTP server on a random local port that answers with prepared responses and records the requests.
 */
final class LocalHttpServer implements AutoCloseable {
    private final HttpServer server;
    private final Map<String, Response> responses = new ConcurrentHashMap<>();
    private final List<URI> requests = new ArrayList<>();
    private final List<String> userAgents = new ArrayList<>();

    LocalHttpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            synchronized (this) {
                requests.add(exchange.getRequestURI());
                userAgents.add(exchange.getRequestHeaders().getFirst("User-Agent"));
            }
            Response response = responses.getOrDefault(exchange.getRequestURI().getPath(),
                    new Response(404, "not found".getBytes(StandardCharsets.UTF_8)));
            exchange.sendResponseHeaders(response.status(), response.body().length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(response.body());
            }
        });
        server.start();
    }

    void respond(String path, int status, byte[] body) {
        responses.put(path, new Response(status, body));
    }

    void respond(String path, String json) {
        respond(path, 200, json.getBytes(StandardCharsets.UTF_8));
    }

    String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    synchronized List<URI> requests() {
        return List.copyOf(requests);
    }

    synchronized List<String> userAgents() {
        return List.copyOf(userAgents);
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private record Response(int status, byte[] body) {
    }
}
