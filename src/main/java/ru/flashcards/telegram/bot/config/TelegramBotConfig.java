package ru.flashcards.telegram.bot.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
@AllArgsConstructor
public class TelegramBotConfig {
    private final TelegramProperties telegramProperties;
    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramProperties.webhookPath()).build();
    }
}
