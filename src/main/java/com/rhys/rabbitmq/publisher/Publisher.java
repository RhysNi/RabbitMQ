package com.rhys.rabbitmq.publisher;

import com.rhys.rabbitmq.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/12/13 11:54 PM
 */
@SpringBootTest
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息（不携带属性）
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/12/14
     */
    @Test
    public void convertAndSend() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "RhysNi.test.Ni", "RhysNi RabbitMQ 实战手册（不携带属性）");
        System.out.println("消息发送成功（不携带属性）");
    }

    /**
     * 发送消息（携带属性）
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/12/14
     */
    @Test
    public void convertAndSendWithProps() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "RhysNi.test.Ni", "RhysNi RabbitMQ 实战手册（携带属性）", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setCorrelationId(UUID.randomUUID().toString());
                return message;
            }
        });
        System.out.println("消息发送成功（携带属性）");
    }
}
