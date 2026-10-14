package ru.flashcards.telegram.bot.config;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    private static final Duration HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(HTTP_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}
