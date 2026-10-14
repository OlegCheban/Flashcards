package ru.flashcards.telegram.component;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.flashcards.telegram.bot.botapi.UpdateDispatcher;
import ru.flashcards.telegram.bot.config.SpeechSynthesisProperties;
import ru.flashcards.telegram.bot.config.TelegramProperties;
import ru.flashcards.telegram.bot.controller.WebhookController;
import ru.flashcards.telegram.bot.sheduler.NotificationManager;

/**
 * Creates every bean of the bot with the database and the AI model mocked: catches wiring errors
 * (missing, ambiguous or circular dependencies) without a database and without registering the webhook.
 */
@SpringJUnitConfig(BotContextWiringTest.WiringConfiguration.class)
class BotContextWiringTest {

    @MockitoBean
    private DSLContext dslContext;
    @MockitoBean
    private ChatModel chatModel;

    @Autowired
    private WebhookController webhookController;
    @Autowired
    private UpdateDispatcher updateDispatcher;
    @Autowired
    private NotificationManager notificationManager;

    @Test
    void createsAllBotBeans() {
        assertNotNull(webhookController);
        assertNotNull(updateDispatcher);
        assertNotNull(notificationManager);
    }

    @Configuration
    @ComponentScan("ru.flashcards.telegram.bot")
    static class WiringConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        TelegramProperties telegramProperties() {
            return new TelegramProperties("https://example.org/webhook", "bot", "token", "secret");
        }

        @Bean
        SpeechSynthesisProperties speechSynthesisProperties() {
            return new SpeechSynthesisProperties("https://example.org/speech", "key", "model", "voice");
        }
    }
}
