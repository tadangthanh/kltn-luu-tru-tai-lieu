package vn.kltn.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@RequiredArgsConstructor
@EnableCaching
public class AppConfig {
}
