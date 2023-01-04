package com.rhys.rabbitmq.publisher;

import com.rhys.rabbitmq.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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

    /**
     * Confirms机制
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2023/1/5
     */
    @Test
    public void convertAndSendConfirm() {
        rabbitTemplate.setConfirmCallback((correlationData, isAck, strCause) -> {
            if (isAck) {
                System.out.println("消息成功发送到交换机中");
            } else {
                System.out.println("消息发送到交换机失败,进行补偿操作");
            }
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "RhysNi.test.Ni", "RhysNi RabbitMQ 实战手册（Confirms机制）");
        System.out.println("消息发送成功（Confirms机制）");
    }

    /**
     * Return机制
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2023/1/5
     */
    @Test
    public void convertAndSendReturn() {
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            String msg = new String(returnedMessage.getMessage().getBody());
            System.out.println("msg:" + msg + "路由到队列失败，进行补偿操作,returnedMessage中可获取到交换机以及路由信息，可重新发送到交换机等。。。");
        });
        //由于RabbitMQConfig中配置的路由规则是 *.test.* 所以这里需要手动将 Rhys.test.Ni 改为 Rhys.test1.Ni 模拟失败操作
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "Rhys.test.Ni", "RhysNi RabbitMQ 实战手册（Return机制）");
        System.out.println("消息发送成功（Return机制）");
    }

    /**
     * 消息持久化(new MessagePostProcessor())
     * MessageDeliveryMode.PERSISTENT 持久化
     * MessageDeliveryMode.NON_PERSISTENT 不持久化
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2023/1/5
     */
    @Test
    public void convertAndSendBasicProps() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "Rhys.test.Ni", "RhysNi RabbitMQ 实战手册（消息持久化）", message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        System.out.println("消息发送成功（消息持久化）");
    }
}
