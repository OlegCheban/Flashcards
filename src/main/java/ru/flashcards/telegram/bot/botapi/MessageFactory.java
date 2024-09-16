package ru.flashcards.telegram.bot.botapi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.handlers.command.CommandMessageHandler;
import ru.flashcards.telegram.bot.botapi.handlers.other.OthersMessagesHandler;
import ru.flashcards.telegram.bot.botapi.handlers.translation.ChangeTranslationMessageHandler;

import java.util.Collections;

@Component
@AllArgsConstructor
public class MessageFactory implements MessageHandlerAbstractFactory<MessageHandler<Message>> {
    private ChangeTranslationMessageHandler changeTranslationMessageHandler;
    private CommandMessageHandler commandMessageHandler;
    private UserMessageTypeBuffer userMessageTypeBuffer;
    //private OthersMessagesHandler othersMessagesHandler;
    @Override
    public MessageHandler<Message> getHandler(Message message) {
        var messageType = userMessageTypeBuffer.getMessageType(message.getChatId());

        switch (messageType){
            case CHANGE_TRANSLATION:
                return changeTranslationMessageHandler;
            case COMMAND:
                return commandMessageHandler;
        }

        return m -> Collections.emptyList();
    }
}
