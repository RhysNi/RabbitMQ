package com.rhys.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rhys.rabbitmq.config.DeadLetteringConfig;
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
public class DeadConsumerListener {

    @RabbitListener(queues = DeadLetteringConfig.QUEUE)
    public void consumer(String msg, Channel channel, Message message) throws IOException {
        System.out.println("正常队列msg:" + msg);
        //tag,requeue
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);

        //tag,multiple,requeue
        // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
    }
}
