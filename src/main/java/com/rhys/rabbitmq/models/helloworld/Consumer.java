package com.rhys.rabbitmq.models.helloworld;

import com.rabbitmq.client.*;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/28 11:14 PM
 */
public class Consumer {
    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建队列 参数要与发布消息时统一
        channel.queueDeclare(Publisher.QUEUE_NAME, false, false, false, null);
        //消费消息
        /*
         * (队列名称,自动确认,当消费者在对应队列中监听到有消息的时候就会执行这个回调)
         * 回调会执行`handleDelivery`方法，这个方法没有具体实现，所以由我们自己来重写一下这个方法即可
         * */
        channel.basicConsume(Publisher.QUEUE_NAME, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                System.out.println("消费到消息:" + new String(body, StandardCharsets.UTF_8));
            }
        });
        System.out.println("开始监听队列:" + Publisher.QUEUE_NAME);
    }
}
