package ru.flashcards.telegram.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param webhookSecret secret token passed to Telegram in setWebhook; Telegram sends it back in the
 *                      {@code X-Telegram-Bot-Api-Secret-Token} header of every update. Empty disables the check.
 */
@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(String webhookPath, String botName, String botToken, String webhookSecret) {

    public boolean hasWebhookSecret() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }
}
