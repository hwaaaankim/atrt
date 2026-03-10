package com.dev.Attraction.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailSendExecutor")
    Executor emailSendExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(6);        // ✅ 병렬 처리 스레드 수 (필요시 조정)
        ex.setMaxPoolSize(12);
        ex.setQueueCapacity(2000);
        ex.setThreadNamePrefix("email-send-");
        ex.initialize();
        return ex;
    }
}