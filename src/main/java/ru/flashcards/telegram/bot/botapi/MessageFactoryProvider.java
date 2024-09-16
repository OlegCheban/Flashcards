package ru.flashcards.telegram.bot.botapi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
@AllArgsConstructor
public class MessageFactoryProvider {
    private ExerciseMessageFactory exerciseMessageFactory;
    private WateringSessionMessageFactory wateringSessionMessageFactory;
    private MessageFactory messageFactory;
    private PrepositionMessageFactory prepositionMessageFactory;

    public MessageHandlerAbstractFactory getFactory(MessageFactoryType messageFactoryType){
        switch (messageFactoryType){
            case EXERCISE:
                return exerciseMessageFactory;
            case WATERING_SESSION:
                return wateringSessionMessageFactory;
            case PREPOSITION:
                return prepositionMessageFactory;
            case OTHER_MESSAGES:
                return messageFactory;
        }

        return (MessageHandlerAbstractFactory<MessageHandler>) (msg) -> m -> Collections.emptyList();
    }
}
