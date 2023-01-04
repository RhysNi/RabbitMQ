package com.rhys.rabbitmq.models.direct;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/29 1:12 AM
 */
public class Publisher {
    public static final String QUEUE_NAME1 = "directQ1";
    public static final String QUEUE_NAME2 = "directQ2";
    public static final String EXCHANGE_NAME = "directExchange";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //构建队列（队列名称,队列是否需要持久化,是否设置为排外队列(只能由一个消费者监听),长时间未使用自动删除，其他参数）
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        //绑定交换机和队列(队列名,交换机名,路由名)
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "error");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "error");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "info");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "warning");
        //发送消息
        channel.basicPublish(EXCHANGE_NAME, "error", null, "routingError".getBytes(StandardCharsets.UTF_8));
        channel.basicPublish(EXCHANGE_NAME, "info", null, "routingInfo".getBytes(StandardCharsets.UTF_8));
        channel.basicPublish(EXCHANGE_NAME, "warning", null, "routingWarning".getBytes(StandardCharsets.UTF_8));
        System.out.println("消息发送成功");
    }
}
