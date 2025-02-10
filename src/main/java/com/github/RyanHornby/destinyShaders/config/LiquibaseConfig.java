package com.github.RyanHornby.destinyShaders.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {
    @Autowired
    @Qualifier("mainDataSource")
    private DataSource mainDataSource;

    @Bean
    @DependsOn
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:db/db-changelog.yml");
        liquibase.setDataSource(mainDataSource);
        return liquibase;
    }
}
