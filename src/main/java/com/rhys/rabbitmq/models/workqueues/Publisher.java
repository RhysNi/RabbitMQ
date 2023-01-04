package com.rhys.rabbitmq.models.workqueues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/28 11:14 PM
 */
public class Publisher {
    public static final String QUEUE_NAME = "work";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建队列
         /*（队列名称,队列是否需要持久化
                    ,是否设置为排外队列(只能由一个消费者监听)
                    ,长时间未使用自动删除
                    ，其他参数（Auto expire | Message TTL | Overflow behaviour
                              Single active consumer | Dead letter exchange | Dead letter routing key
                              Max length | Max length bytes
                              Maximum priority | Lazy mode | Version | Master locator ））
        */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //发布消息
        /*
         * (交换机名称(默认空字符串),队列名,其他参数,消息内容
         * */
        for (int i = 0; i < 10; i++) {
            byte[] msg = ("work" + i).getBytes(StandardCharsets.UTF_8);
            channel.basicPublish("", QUEUE_NAME, null, msg);
            System.out.println("消息" + ("work" + i) + "发送成功");
        }
    }
}
