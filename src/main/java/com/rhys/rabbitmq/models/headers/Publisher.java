package com.rhys.rabbitmq.models.headers;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/29 1:12 AM
 */
public class Publisher {
    public static final String QUEUE_NAME1 = "headersQ1";
    public static final String QUEUE_NAME2 = "headersQ2";
    public static final String EXCHANGE_NAME = "headersExchange";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.HEADERS);
        //构建队列（队列名称,队列是否需要持久化,是否设置为排外队列(只能由一个消费者监听),长时间未使用自动删除，其他参数）
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);

        //构建交换机和队列绑定规则
        Map<String, Object> params1 = new HashMap<>();
        params1.put("x-match", "all");
        params1.put("name", "RhysNi");
        params1.put("age", "24");

        Map<String, Object> params2 = new HashMap<>();
        params2.put("x-match", "any");
        params2.put("name", "RhysNi");
        params2.put("age", "23");

        //绑定交换机和队列
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "", params1);
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "", params2);

        //构建headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("name", "RhysNi");
        headers.put("age", "24");

        AMQP.BasicProperties properties = new AMQP.BasicProperties()
                .builder()
                .headers(headers)
                .build();

        //发送消息
        channel.basicPublish(EXCHANGE_NAME, "", properties, "测试Headers Exchange".getBytes(StandardCharsets.UTF_8));
        System.out.println("消息发送成功");
    }
}
