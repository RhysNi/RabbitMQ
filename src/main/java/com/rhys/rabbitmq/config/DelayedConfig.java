package com.rhys.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/1/10 3:34 AM
 */
@Configuration
public class DelayedConfig {
    //正常交换机
    public static final String DELAYED_EXCHANGE = "delayed-exchange";
    //正常队列
    public static final String DELAYED_QUEUE = "delayed-queue";
    //正常路由key
    public static final String DELAYED_ROUTING_KEY = "delayed.test.#";

    @Bean
    public Exchange delayedExchange() {
        Map<String, Object> args = new HashMap<>(1);
        //指定延迟交换机类型
        args.put("x-delayed-type", "topic");

        Exchange exchange = new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
        return exchange;
    }

    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable(DELAYED_QUEUE).build();
    }

    @Bean
    public Binding delayedBinding(Queue delayedQueue, Exchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }
}
