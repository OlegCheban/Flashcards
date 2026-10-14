package ru.flashcards.telegram.bot.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import ru.flashcards.telegram.bot.botapi.phrasalverb.PhrasalVerbCatalog;
import ru.flashcards.telegram.bot.botapi.phrasalverb.PhrasalVerbQuestion;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionCatalog;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionExercise;
import ru.flashcards.telegram.bot.botapi.training.TrainingSessionStore;

/**
 * Content and session stores of the trainings. The content is validated on load, invalid content fails the start.
 */
@Configuration
public class TrainingConfig {
    private static final String PREPOSITIONS_RESOURCE = "prepositions.yml";
    private static final String PHRASAL_VERBS_RESOURCE = "phrasal-verbs.yml";

    @Bean
    public PrepositionCatalog prepositionCatalog() throws IOException {
        try (InputStream exercises = new ClassPathResource(PREPOSITIONS_RESOURCE).getInputStream()) {
            return PrepositionCatalog.fromYaml(exercises);
        }
    }

    @Bean
    public PhrasalVerbCatalog phrasalVerbCatalog() throws IOException {
        try (InputStream verbs = new ClassPathResource(PHRASAL_VERBS_RESOURCE).getInputStream()) {
            return PhrasalVerbCatalog.fromYaml(verbs);
        }
    }

    @Bean
    public TrainingSessionStore<PrepositionExercise> prepositionSessionStore() {
        return new TrainingSessionStore<>();
    }

    @Bean
    public TrainingSessionStore<PhrasalVerbQuestion> phrasalVerbSessionStore() {
        return new TrainingSessionStore<>();
    }
}
