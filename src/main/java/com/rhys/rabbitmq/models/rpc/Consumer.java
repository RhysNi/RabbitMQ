package com.rhys.rabbitmq.models.rpc;

import com.rabbitmq.client.*;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.io.IOException;

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
        channel.queueDeclare(Publisher.PUBLISHER_QUEUE, false, false, false, null);
        channel.queueDeclare(Publisher.CONSUMER_QUEUE, false, false, false, null);

        //消费消息(队列名称,自动确认,当消费者在对应队列中监听到有消息的时候就会执行这个回调)
        DefaultConsumer callback = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("消费者获取到消息：" + new String(body,"UTF-8"));
                String resp = "获取到了client发出的请求，这里是响应的信息";
                String respQueueName = properties.getReplyTo();
                String uuid = properties.getCorrelationId();
                AMQP.BasicProperties props = new AMQP.BasicProperties()
                        .builder()
                        .correlationId(uuid)
                        .build();
                channel.basicPublish("",respQueueName,props,resp.getBytes());
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channel.basicConsume(Publisher.PUBLISHER_QUEUE,false,callback);
        System.out.println("开始监听队列");

        System.in.read();
    }
}
