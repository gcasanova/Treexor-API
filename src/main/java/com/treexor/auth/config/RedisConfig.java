package com.treexor.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.treexor.auth.entities.Profile;

@Configuration
public class RedisConfig {

    @Bean
    RedisAtomicLong redisAtomicLong() {
        return new RedisAtomicLong("accounts", jedisConnectionFactory());
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    RedisConnection redisConnection() {
        return jedisConnectionFactory().getConnection();
    }

    @Bean(name = "redisProfileTemplate")
    RedisTemplate<String, Profile> redisEventTemplate() {
        final RedisTemplate<String, Profile> template = new RedisTemplate<String, Profile>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Profile.class));
        template.afterPropertiesSet();
        return template;
    }
}
