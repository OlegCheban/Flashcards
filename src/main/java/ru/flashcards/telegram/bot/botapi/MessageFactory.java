package ru.flashcards.telegram.bot.botapi;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.handlers.command.CommandMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.translation.ChangeTranslationMessageHandler;

@Component
@RequiredArgsConstructor
public class MessageFactory implements MessageHandlerAbstractFactory {
    private static final String COMMAND_PREFIX = "/";

    private final ChangeTranslationMessageHandler changeTranslationMessageHandler;
    private final CommandMessageHandler commandMessageHandler;
    private final UserMessageTypeBuffer userMessageTypeBuffer;

    /**
     * A command sent while the bot waits for a new translation cancels the pending request,
     * so the command is not saved as a translation.
     */
    @Override
    public MessageHandler getHandler(Message message) {
        Long chatId = message.getChatId();
        if (message.getText().startsWith(COMMAND_PREFIX)) {
            userMessageTypeBuffer.removeRequest(chatId);
            return commandMessageHandler;
        }

        return switch (userMessageTypeBuffer.getMessageType(chatId)) {
            case CHANGE_TRANSLATION -> changeTranslationMessageHandler;
            case COMMAND -> commandMessageHandler;
        };
    }
}
