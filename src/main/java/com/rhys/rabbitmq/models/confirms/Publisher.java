package com.rhys.rabbitmq.models.confirms;

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
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

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

        //发送消息
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));

        System.in.read();
    }
}
