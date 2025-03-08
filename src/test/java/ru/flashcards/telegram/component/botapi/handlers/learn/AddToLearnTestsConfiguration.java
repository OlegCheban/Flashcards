package ru.flashcards.telegram.component.botapi.handlers.learn;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;

@TestConfiguration
public class AddToLearnTestsConfiguration {

    @Bean
    public AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler(DataLayerObject dL, UserProfileFlashcardsDao uP, SuggestFlashcard sF, FlashcardsDao fD){
        return new AddToLearnAndNextCallbackHandler(dL, uP, sF, fD);
    }

    @Bean
    public AddToLearnCallbackHandler addToLearnCallbackHandler(DataLayerObject dL, UserProfileFlashcardsDao uP, FlashcardsDao fD){
        return new AddToLearnCallbackHandler(dL, uP, fD);
    }
}
