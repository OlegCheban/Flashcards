package ru.flashcards.telegram.bot.config;

import java.util.regex.Pattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {
    /**
     * Telegram's restriction for secret_token.
     */
    private static final Pattern WEBHOOK_SECRET_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,256}");

    private final TelegramProperties telegramProperties;

    @Bean
    public SetWebhook setWebhookInstance() {
        SetWebhook setWebhook = SetWebhook.builder().url(telegramProperties.webhookPath()).build();
        if (telegramProperties.hasWebhookSecret()) {
            if (!WEBHOOK_SECRET_PATTERN.matcher(telegramProperties.webhookSecret()).matches()) {
                throw new IllegalStateException(
                        "telegram.webhook-secret must be 1-256 characters of A-Z, a-z, 0-9, '_' or '-'");
            }
            setWebhook.setSecretToken(telegramProperties.webhookSecret());
        } else {
            log.warn("telegram.webhook-secret is not configured: incoming webhook requests are not authenticated");
        }
        return setWebhook;
    }
}
