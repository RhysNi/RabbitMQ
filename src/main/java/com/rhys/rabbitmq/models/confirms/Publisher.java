package com.rhys.rabbitmq.models.confirms;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Publisher {
    public static final String QUEUE_NAME = "confirmsQueue";
    public static final String EXCHANGE_NAME = "confirmsExchange";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();

        //构建channel
        Channel channel = connection.createChannel();

        //声明队列
        //queueDeclare(queue, durable, exclusive, autoDelete, arguments)
        //durable:true 队列持久化
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //准备消息
        String msg = "hello";

        //开启confirms
        channel.confirmSelect();

        //confirms异步回调
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long l, boolean b) {
                System.out.println("消息发送成功：" + msg);
            }

            @Override
            public void handleNack(long l, boolean b) throws IOException {
                System.out.println("消息发送失败，Retry...");
            }
        });

        //return机制 （确认消息是否路由到了队列）
        //消息没有路由到队列时会回调return方法
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, basicProperties, bytes) -> System.out.println("消息已送达到交换机，但未路由到指定队列，可做补偿机制..."));

        //设置消息持久化
        //服务器宕机/MQ服务重启消息依旧会存在队列中
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .deliveryMode(2)
                .build();

        //在发送消息时需要将'mandatory'属性设置为ture,代表开启return机制
        //basicPublish(交换机, 队列, mandatory值,属性, 消息内容)
        //QUEUE_NAME+"111"，手动调整为不存在的队列，否则无法回调return方法
        channel.basicPublish("", QUEUE_NAME, true, properties, msg.getBytes(StandardCharsets.UTF_8));

        System.in.read();
    }
}
