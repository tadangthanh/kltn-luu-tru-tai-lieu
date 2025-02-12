package vn.kltn.configuration;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@NoArgsConstructor
public class RedisConfiguration {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Value("${spring.data.redis.connect-timeout}")
    private int redisTimeout;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(100);
        poolConfig.setMinIdle(16);
        poolConfig.setJmxEnabled(false);
        return poolConfig;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig poolConfig) {
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(poolConfig);
        jedisConFactory.setHostName(this.redisHost);
        jedisConFactory.setPort(this.redisPort);
        jedisConFactory.setTimeout(this.redisTimeout);
        return jedisConFactory;
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public JedisPool jedisPool(JedisPoolConfig poolConfig) {
        return new JedisPool(poolConfig, this.redisHost, this.redisPort);
    }
}
