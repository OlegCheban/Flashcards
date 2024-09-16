package ru.flashcards.telegram.bot.config.jooq;

import lombok.RequiredArgsConstructor;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PersistenceContext {
    private final DataSourceConnectionProvider dataSourceConnectionProvider;
    @Bean
    public ExceptionTranslator exceptionTransformer() {
        return new ExceptionTranslator();
    }
    @Bean
    public DefaultConfiguration configuration() {
        DefaultConfiguration JooqConfiguration = new DefaultConfiguration();
        JooqConfiguration.set(dataSourceConnectionProvider);
        JooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
        JooqConfiguration.set(SQLDialect.POSTGRES);
        return JooqConfiguration;
    }
    @Bean
    public DefaultDSLContext dsl() {
        return new DefaultDSLContext(configuration());
    }
}
