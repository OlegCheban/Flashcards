package ru.flashcards.telegram.component.botapi.handlers.learn;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;

@TestConfiguration
public class AddToLearnTestsConfiguration {
    @Bean
    public AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler(DataLayerObject dL, SuggestFlashcard sF){
        return new AddToLearnAndNextCallbackHandler(dL, sF);
    }
    @Bean
    public AddToLearnCallbackHandler addToLearnCallbackHandler(DataLayerObject dL){
        return new AddToLearnCallbackHandler(dL);
    }
}
