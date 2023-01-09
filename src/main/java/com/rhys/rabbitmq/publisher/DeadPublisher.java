package com.rhys.rabbitmq.publisher;

import com.rhys.rabbitmq.config.DeadLetteringConfig;
import com.rhys.rabbitmq.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
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
public class DeadPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/12/14
     */
    @Test
    public void convertAndSend() {
        rabbitTemplate.convertAndSend(DeadLetteringConfig.EXCHANGE, "Rhys.test.Ni", "RhysNi RabbitMQ 实战手册（死信测试）");
        System.out.println("消息发送成功");
    }

    @Test
    public void convertAndSendExpire() {
        rabbitTemplate.convertAndSend(DeadLetteringConfig.EXCHANGE, "Rhys.test.Ni", "RhysNi RabbitMQ 实战手册（死信测试）", message -> {
            //3s过后没被消费被转入死信队列
            message.getMessageProperties().setExpiration("3000");
            return message;
        });
        System.out.println("消息发送成功");
    }
}
