package vn.kltn.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {
//    @Bean(name = "taskExecutor")
//    public Executor taskExecutor() {
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(25);
//        executor.setThreadNamePrefix("Async-");
//        executor.initialize();
//        return executor;
//    }
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor delegateExecutor = new ThreadPoolTaskExecutor();
    delegateExecutor.setCorePoolSize(5);
    delegateExecutor.setMaxPoolSize(10);
    delegateExecutor.setQueueCapacity(25);
    delegateExecutor.setThreadNamePrefix("Async-");
    delegateExecutor.initialize();

    // Bao bọc executor để truyền SecurityContext đúng cách
    return new DelegatingSecurityContextAsyncTaskExecutor(delegateExecutor);
}
}
