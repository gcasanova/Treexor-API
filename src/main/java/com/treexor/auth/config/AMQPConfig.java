package com.treexor.auth.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.treexor.common.converters.JsonEasyConverter;

@Configuration
public class AMQPConfig {

    @Value("${login.queue}")
    private String loginQueue;

    @Value("${logout.queue}")
    private String logoutQueue;

    @Value("${register.queue}")
    private String registerQueue;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public RabbitTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(new JsonEasyConverter());
        rabbitTemplate.setReplyTimeout(60000);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(new JsonEasyConverter());
        return factory;
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPublisherReturns(true);
        factory.setPublisherConfirms(true);
        return factory;
    }

    @Bean
    public Queue loginQueue() {
        return new Queue(loginQueue, true);
    }

    @Bean
    public Queue logoutQueue() {
        return new Queue(logoutQueue, true);
    }

    @Bean
    public Queue registerQueue() {
        return new Queue(registerQueue, true);
    }
}
