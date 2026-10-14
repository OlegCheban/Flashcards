package ru.flashcards.telegram.component.botapi.handlers.learn;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import ru.flashcards.telegram.bot.botapi.SuggestFlashcard;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;

@TestConfiguration
public class AddToLearnTestsConfiguration {

    @Bean
    public AddToLearnCallbackHandler addToLearnCallbackHandler(
            UserProfileFlashcardsDao userProfileFlashcardsDao, FlashcardsDao flashcardsDao) {
        return new AddToLearnCallbackHandler(userProfileFlashcardsDao, flashcardsDao);
    }

    @Bean
    public AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler(
            AddToLearnCallbackHandler addToLearnCallbackHandler, SuggestFlashcard suggestFlashcard) {
        return new AddToLearnAndNextCallbackHandler(addToLearnCallbackHandler, suggestFlashcard);
    }
}
