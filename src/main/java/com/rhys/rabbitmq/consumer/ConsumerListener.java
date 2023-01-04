package com.rhys.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rhys.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/12/13 11:54 PM
 */
@Component
public class ConsumerListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumer(String msg, Channel channel, Message message) throws IOException {
        System.out.println("msg:" + msg);
        String correlationId = message.getMessageProperties().getCorrelationId();
        System.out.println("correlationId:" + correlationId);
        //手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
