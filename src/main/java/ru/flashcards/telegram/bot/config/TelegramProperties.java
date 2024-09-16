package ru.flashcards.telegram.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties (String apiUrl, String webhookPath, String botName, String botToken){}
