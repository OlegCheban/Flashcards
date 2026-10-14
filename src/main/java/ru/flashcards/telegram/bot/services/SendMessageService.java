package ru.flashcards.telegram.bot.services;

import java.io.Serializable;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.FlashcardBot;

/**
 * Executes outgoing Telegram Bot API calls.
 * A failed call is logged and does not interrupt the caller: one undeliverable message
 * (e.g. the user blocked the bot) must not break the remaining replies or a notification batch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendMessageService {
    private final FlashcardBot flashcardBot;

    public <T extends Serializable> void send(BotApiMethod<T> method) {
        try {
            flashcardBot.execute(method);
        } catch (TelegramApiException e) {
            log.warn("Telegram API call failed: method={}", method.getMethod(), e);
        }
    }

    /**
     * Telegram downloads the audio by URL itself.
     */
    public void sendAudio(Long chatId, String audioUrl) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setAudio(new InputFile(audioUrl));
        try {
            flashcardBot.execute(sendAudio);
        } catch (TelegramApiException e) {
            log.warn("Telegram API call failed: method=sendAudio, chatId={}", chatId, e);
        }
    }
}
