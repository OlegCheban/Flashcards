package ru.flashcards.telegram.jooq.test;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;

import javax.sql.DataSource;

@TestConfiguration
@PropertySource({"classpath:application-test.properties"})
public class JooqTestConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    @Bean
    public UserProfileFlashcardsDao userProfileFlashcards(DSLContext dslContext) {
        return new UserProfileFlashcardsDao(dslContext);
    }

    @Bean
    public LearningExercisesDao learningExercises(DSLContext dslContext) {
        return new LearningExercisesDao(dslContext);
    }

    @Bean
    public FlashcardsDao flashcards(DSLContext dslContext) {
        return new FlashcardsDao(dslContext);
    }
}
