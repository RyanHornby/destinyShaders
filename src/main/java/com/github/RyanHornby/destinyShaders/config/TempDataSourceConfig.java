package com.github.RyanHornby.destinyShaders.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.github.RyanHornby.destinyShaders.repository.temp",
    entityManagerFactoryRef = "tempEntityManagerFactory",
    transactionManagerRef = "tempTransactionManager")
public class TempDataSourceConfig {
    @Autowired
    private Environment env;
    @Autowired
    private String tempDbLocation;

    @Bean
    public DataSourceProperties tempDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource tempDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("driverClassName"));
        dataSource.setUrl("jdbc:sqlite:" + tempDbLocation);
        return dataSource;
    }

    @Bean(name = "tempEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tempEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(tempDataSource())
                .packages("com.github.RyanHornby.destinyShaders.model.entity")
                .build();
    }

    @Bean
    public PlatformTransactionManager tempTransactionManager(final @Qualifier("tempEntityManagerFactory")
                                                                 LocalContainerEntityManagerFactoryBean tempEntityManagerFactory) {
        return new JpaTransactionManager(tempEntityManagerFactory.getObject());
    }
}
