package ru.flashcards.telegram.component.botapi.handlers.learn;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;

@TestConfiguration
public class AddToLearnTestsConfiguration {

    @Bean
    public AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler(UserProfileFlashcardsDao uP, SuggestFlashcard sF, FlashcardsDao fD){
        return new AddToLearnAndNextCallbackHandler(uP, sF, fD);
    }

    @Bean
    public AddToLearnCallbackHandler addToLearnCallbackHandler(UserProfileFlashcardsDao uP, FlashcardsDao fD){
        return new AddToLearnCallbackHandler(uP, fD);
    }
}
