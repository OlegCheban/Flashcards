package ru.flashcards.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.flashcards.telegram.bot.config.TelegramProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TelegramProperties.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
