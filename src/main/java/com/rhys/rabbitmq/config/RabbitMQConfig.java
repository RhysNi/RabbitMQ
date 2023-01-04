package com.rhys.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "testExchange";
    public static final String QUEUE = "testQueue";
    public static final String ROUTING_KEY = "*.test.*";

    /**
     * 定义交换机
     *
     * @param
     * @return org.springframework.amqp.core.Exchange
     * @author Rhys.Ni
     * @date 2022/12/13
     */
    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).build();
    }

    /**
     * 定义队列
     *
     * @param
     * @return org.springframework.amqp.core.Queue
     * @author Rhys.Ni
     * @date 2022/12/13
     */
    @Bean
    public Queue queue() {
        //durable：持久化队列
        //nonDurable：非持久化队列
        return QueueBuilder.durable(QUEUE).build();
    }

    /**
     * 将交换机与队列通过RoutingKey建立绑定关系
     *
     * @param exchange
     * @param queue
     * @return org.springframework.amqp.core.Binding
     * @author Rhys.Ni
     * @date 2022/12/13
     */
    @Bean
    public Binding binding(Exchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
