package ru.flashcards.telegram.bot.botapi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@AllArgsConstructor
public class CallbackFactoryProvider {
    private CallbackFactory callbackFactory;

    public CallbackHandlerAbstractFactory getFactory(MessageFactoryType messageFactoryType){
        switch (messageFactoryType){
            case CALLBACK:
                return callbackFactory;
        }

        return (CallbackHandlerAbstractFactory<MessageHandler>) (callbackDataJson) -> m -> Collections.emptyList();
    }
}
