package com.rhys.rabbitmq.models.rpc;

import com.rabbitmq.client.*;
import com.rhys.rabbitmq.utils.ConnectionUtil;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/29 2:53 AM
 */
public class Publisher {
    public static final String PUBLISHER_QUEUE = "rpc_publisher";
    public static final String CONSUMER_QUEUE = "rpc_consumer";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建队列
        // （队列名称,队列是否需要持久化,是否设置为排外队列(只能由一个消费者监听),长时间未使用自动删除,其他参数）
        channel.queueDeclare(PUBLISHER_QUEUE, false, false, false, null);
        channel.queueDeclare(CONSUMER_QUEUE, false, false, false, null);
        //发布消息(交换机名称(默认空字符串),队列名,其他参数,消息内容
        String message = "Hello RPC!";
        String uuid = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties()
                .builder()
                .replyTo(CONSUMER_QUEUE)
                .correlationId(uuid)
                .build();
        channel.basicPublish("",PUBLISHER_QUEUE,props,message.getBytes());

        channel.basicConsume(CONSUMER_QUEUE,false,new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String id = properties.getCorrelationId();
                if(id != null && id.equalsIgnoreCase(uuid)){
                    System.out.println("接收到服务端的响应：" + new String(body,"UTF-8"));
                }
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        });
        System.out.println("消息发送成功！");

        System.in.read();
    }
}
