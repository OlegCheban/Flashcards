package ru.flashcards.telegram.bot.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.botapi.UpdateDispatcher;
import ru.flashcards.telegram.bot.config.TelegramProperties;
import ru.flashcards.telegram.bot.services.SendMessageService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {
    private static final String SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private final UpdateDispatcher updateDispatcher;
    private final SendMessageService sendMessageService;
    private final TelegramProperties telegramProperties;
    private final Environment environment;

    /**
     * Always answers 200 for authenticated requests, otherwise Telegram would redeliver a failing update forever.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> onUpdateReceived(
            @RequestHeader(value = SECRET_TOKEN_HEADER, required = false) String secretToken,
            @RequestBody Update update) {
        if (!isAuthenticated(secretToken)) {
            log.warn("Rejected webhook request with invalid secret token: updateId={}", update.getUpdateId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            updateDispatcher.dispatch(update).forEach(sendMessageService::send);
        } catch (RuntimeException e) {
            log.error("Failed to process update: updateId={}", update.getUpdateId(), e);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/version")
    public ResponseEntity<String> version() {
        return ResponseEntity.ok(environment.getProperty("app.version", "unknown"));
    }

    private boolean isAuthenticated(String secretToken) {
        if (!telegramProperties.hasWebhookSecret()) {
            return true;
        }
        return secretToken != null && MessageDigest.isEqual(
                secretToken.getBytes(StandardCharsets.UTF_8),
                telegramProperties.webhookSecret().getBytes(StandardCharsets.UTF_8));
    }
}
