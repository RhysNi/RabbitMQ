package com.rhys.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/1/9 11:50 PM
 */
@Configuration
public class DeadLetteringConfig {
    //正常交换机
    public static final String EXCHANGE = "test-exchange";
    //死信交换机
    public static final String DEAD_EXCHANGE = "dead-test-exchange";


    //正常队列
    public static final String QUEUE = "test-queue";
    //死信队列
    public static final String DEAD_QUEUE = "dead-test-queue";


    //正常路由key
    public static final String ROUTING_KEY = "*.test.#";
    //死信路由key
    public static final String DEAD_ROUTING_KEY = "dead.test.*";


    @Bean
    public Exchange testExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).build();
    }

    /**
     * deadLetterRoutingKey消息变为死信时路由key需要被修改成什么
     * @author Rhys.Ni
     * @date 2023/1/10
     * @param
     * @return org.springframework.amqp.core.Queue
     */
    @Bean
    public Queue testQueue() {
        return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_EXCHANGE).deadLetterRoutingKey("dead.test.ni")
                //3s过后没被消费被转入死信队列
                // .ttl(3000)
                //设置队列最大长度为1模拟超出队列长度，后续路由过来的消息转死信场景
                .maxLength(1)
                .build();
    }

    @Bean
    public Binding testBinding(Queue testQueue, Exchange testExchange) {
        return BindingBuilder.bind(testQueue).to(testExchange).with(ROUTING_KEY).noargs();
    }



    @Bean
    public Exchange deadTestExchange() {
        return ExchangeBuilder.topicExchange(DEAD_EXCHANGE).build();
    }

    @Bean
    public Queue deadTestQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding deadBinding(Queue deadTestQueue, Exchange deadTestExchange) {
        return BindingBuilder.bind(deadTestQueue).to(deadTestExchange).with(DEAD_ROUTING_KEY).noargs();
    }
}
