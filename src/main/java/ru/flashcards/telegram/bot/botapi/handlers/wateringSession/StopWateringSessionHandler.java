package ru.flashcards.telegram.bot.botapi.handlers.wateringSession;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionTiming;

@Component
@RequiredArgsConstructor
public class StopWateringSessionHandler implements MessageHandler {
    private final UserModeSettings userModeSettings;
    private final WateringSessionTiming wateringSessionTiming;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        Long chatId = message.getChatId();
        userModeSettings.removeMode(chatId);
        wateringSessionTiming.stop(chatId);

        return List.of(BotMessages.withoutKeyboard(BotMessages.text(chatId, "Keep learning!")));
    }
}
