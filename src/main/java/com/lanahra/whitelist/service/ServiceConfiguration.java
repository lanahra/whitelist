package com.lanahra.whitelist.service;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ServiceConfiguration
 * Setup executor for concurrent processing in Service, set pool size to number
 * of cores available.
 *
 * @see Service
 */
@Configuration
@EnableAsync
public class ServiceConfiguration {

    @Bean
    public Executor asyncExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores);
        executor.setThreadNamePrefix("whitelist-");
        executor.initialize();
        return executor;
    }
}
