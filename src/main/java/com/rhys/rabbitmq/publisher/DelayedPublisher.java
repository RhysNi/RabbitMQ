package com.rhys.rabbitmq.publisher;

import com.rhys.rabbitmq.config.DeadLetteringConfig;
import com.rhys.rabbitmq.config.DelayedConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/12/13 11:54 PM
 */
@SpringBootTest
public class DelayedPublisher {
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
        rabbitTemplate.convertAndSend(DelayedConfig.DELAYED_EXCHANGE, "delayed.test.Ni", "RhysNi RabbitMQ 实战手册（延迟交换机测试）");
        System.out.println("消息发送成功");
    }

    @Test
    public void convertAndSendExpire1() {
        rabbitTemplate.convertAndSend(DelayedConfig.DELAYED_EXCHANGE, "delayed.test.Ni", "RhysNi RabbitMQ 实战手册（延迟交换机测试）", message -> {
            //3s过后没被消费被转入死信队列
            message.getMessageProperties().setDelay(30000);
            return message;
        });
        System.out.println("消息发送成功");
    }

    @Test
    public void convertAndSendExpire2() {
        rabbitTemplate.convertAndSend(DelayedConfig.DELAYED_EXCHANGE, "delayed.test.Ni", "RhysNi RabbitMQ 实战手册（延迟交换机测试）", message -> {
            //3s过后没被消费被转入死信队列
            message.getMessageProperties().setDelay(3000);
            return message;
        });
        System.out.println("消息发送成功");
    }
}
