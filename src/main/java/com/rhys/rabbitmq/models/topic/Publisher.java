package com.rhys.rabbitmq.models.topic;

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
    public static final String QUEUE_NAME1 = "topicQ1";
    public static final String QUEUE_NAME2 = "topicQ2";
    public static final String EXCHANGE_NAME = "topicExchange";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //构建队列（队列名称,队列是否需要持久化,是否设置为排外队列(只能由一个消费者监听),长时间未使用自动删除，其他参数）
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        //绑定交换机和队列(队列名,交换机名,路由名)，*：占位符   #：通配符
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "*.orange.*");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "*.*.rabbit");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "lazy.#");
        //发送消息
        channel.basicPublish(EXCHANGE_NAME, "rhys.orange.rabbit", null, "测试占位符*.orange.*和*.*.rabbit".getBytes(StandardCharsets.UTF_8));
        channel.basicPublish(EXCHANGE_NAME, "rhys.ni.rabbit", null, "测试占位符*.*.rabbit".getBytes(StandardCharsets.UTF_8));
        channel.basicPublish(EXCHANGE_NAME, "lazy.rhys", null, "测试通配符lazy.#".getBytes(StandardCharsets.UTF_8));
        System.out.println("消息发送成功");
    }
}
