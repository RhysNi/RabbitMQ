package com.rhys.rabbitmq.models.direct;

import com.rabbitmq.client.*;
import com.rhys.rabbitmq.utils.ConnectionUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/28 11:14 PM
 */
public class Consumer {

    @Test
    public void consumer1() throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建队列 参数要与发布消息时统一
        channel.queueDeclare(Publisher.QUEUE_NAME1, false, false, false, null);
        //设置消息流控
        channel.basicQos(1);
        //消费消息
        /*
         * (队列名称,自动确认,当消费者在对应队列中监听到有消息的时候就会执行这个回调)
         * 回调会执行`handleDelivery`方法，这个方法没有具体实现，所以由我们自己来重写一下这个方法即可
         * */
        channel.basicConsume(Publisher.QUEUE_NAME1, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("consumer1消费到消息:" + new String(body, StandardCharsets.UTF_8));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
        System.out.println("consumer1开始监听队列:" + Publisher.QUEUE_NAME1);
        System.in.read();
    }

    @Test
    public void consumer2() throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建队列 参数要与发布消息时统一
        channel.queueDeclare(Publisher.QUEUE_NAME2, false, false, false, null);
        //设置消息流控
        channel.basicQos(1);
        //消费消息
        /*
         * (队列名称,自动确认,当消费者在对应队列中监听到有消息的时候就会执行这个回调)
         * 回调会执行`handleDelivery`方法，这个方法没有具体实现，所以由我们自己来重写一下这个方法即可
         *
         * 如果需要让性能高的消费者尽可能多的消费，就要关闭自动ACK:false，改为手动ACK
         * basicAck(消费者标识(不同消费者是不一样的),是否是批量操作)
         * */
        channel.basicConsume(Publisher.QUEUE_NAME2, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("consumer2消费到消息:" + new String(body, StandardCharsets.UTF_8));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
        System.out.println("consumer2开始监听队列:" + Publisher.QUEUE_NAME2);
        System.in.read();
    }
}
