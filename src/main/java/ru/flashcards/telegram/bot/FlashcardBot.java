package ru.flashcards.telegram.bot;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.flashcards.telegram.bot.config.TelegramProperties;
@RestController
public class FlashcardBot extends SpringWebhookBot {

    private final TelegramProperties telegramProperties;

    public FlashcardBot(SetWebhook setWebhook, TelegramProperties telegramProperties) {
        super(setWebhook);
        this.telegramProperties = telegramProperties;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(@RequestBody Update update) {
        //this bot must be able to send several answers per a request
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public String getBotPath() {
        return telegramProperties.webhookPath();
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.botName();
    }

    @Override
    public String getBotToken() {
        return telegramProperties.botToken();
    }
}