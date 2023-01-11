# RabbitMQ

## 简介

> - **RabbitMQ**是实现了高级消息队列协议（AMQP）的开源消息代理软件（亦称面向消息的中间件）。RabbitMQ服务器是用[Erlang](https://baike.baidu.com/item/Erlang)语言编写的，而集群和故障转移是构建在[开放电信平台](https://baike.baidu.com/item/开放电信平台)框架上的。所有主要的[编程语言](https://baike.baidu.com/item/编程语言/9845131)均有与代理接口通讯的[客户端](https://baike.baidu.com/item/客户端/101081)库。
>
> - 由于RabbitMQ是基于AMQP协议开发，所以RabbitMQ支持很多基于AMQP协议的功能，比如SpringCloud Bus
> - 由于RabbitMQ是基于Erlang编写，Erlang被称为面向并发编程的语言，并发能力极强，在众多的MQ中，RabbitMQ的延迟特别低，在微秒级别，所以一般的业务处理RabbitMQ比Kafka和RocketMQ更有优势。

## AMQP协议

> `AMQP 0-9-1（高级消息队列协议）`是一种符合客户端应用程序的消息传递协议与符合传递标准的消息中间件进行通信代理。 

### AMQP 0-9-1 模型

![通过交换和队列发布从发布者到消费者的路径](https://i0.hdslb.com/bfs/album/2283c15961e377d3b177adeda2217fbfa85077f2.png)

> 根据`AMQP 0-9-1 模型`可得,我们首先得具备消息的发布者`Publisher`和消息消费方`Consumer`，然后由发布者将消息发布到`RabiitMQ`的交换机(Exchange)上，再由交换机根据某些路由规则(Routes)将消息发送到某一个队列(Queue)上，最后由监听这个队列的消费者对消息进行消费

## RabbitMQ整体架构

![image-20221122012721280](https://i0.hdslb.com/bfs/album/da90c280f50a939d6336aa6afed481c527ff8adf.png)

> 在`RabbitMQ`服务中存在一个或多个`Virtual Host`，在每个`Virtual Host`中又存在一个/多个`Exchange`，`Publisher`要与`Virtual Host`建立连接，然后通过`Channel`将消息发送到这些`Exchange`交换机中，再由交换机通过不同的路由规则将消息递送到对应的消息队列中，最后由`Consumer`与`Virtual Host`建立连接，再通过`Channel`管道将它所监听的队列中的消息拿出来进行消费

## [七种通讯方式](https://www.rabbitmq.com/getstarted.html)

### [Hello World](https://www.rabbitmq.com/tutorials/tutorial-one-python.html)

> 一个生产者，一个消费者，使用默认交换机，自行创建队列

![image-20221129001604383](https://i0.hdslb.com/bfs/album/da93cdd1c6e084fac49e9e3a3d80d7a5f99ad59d.png)

#### 示例代码

##### 创建连接工具类

```java
package com.rhys.rabbitmq.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/11/22 3:02 AM
 */
public class ConnectionUtil {
    public static final String RABBITMQ_HOST = "127.0.0.1";

    public static final int RABBITMQ_PORT = 5672;

    public static final String RABBITMQ_USERNAME = "guest";

    public static final String RABBITMQ_PASSWORD = "guest";

    public static final String RABBITMQ_VIRTUAL_HOST = "/";

    /**
     * 构建RabbitMQ的连接对象
     *
     * @return Connection
     * @author Rhys.Ni
     * @date 2022/11/22
     */
    public static Connection getConnection() throws Exception {
        //1. 创建Connection工厂
        ConnectionFactory factory = new ConnectionFactory();

        //2. 设置RabbitMQ的连接信息
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(RABBITMQ_PORT);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);
        factory.setVirtualHost(RABBITMQ_VIRTUAL_HOST);

        //3. 返回连接对象
        Connection connection = factory.newConnection();
        return connection;
    }
}

```

##### Publisher

```java
package com.rhys.rabbitmq.models.helloworld;

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
    public static final String QUEUE_NAME = "hello";

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
        byte[] msg = "hello".getBytes(StandardCharsets.UTF_8);
        channel.basicPublish("", QUEUE_NAME, null, msg);

        System.out.println("消息发送成功");
    }
}
```

##### Consumer

```java
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
```

### [Work Queues](https://www.rabbitmq.com/tutorials/tutorial-two-python.html)

> 在多个消费者之间分配任务
>
> - 一个队列中的消息只会被一个消费者成功的消费
> - 默认情况下，队列会将消息轮询交给不同的消费者进行消费
> - 消费者拿到消息后需要给MQ一个ACK，代表消费者已经拿到消息

![image-20221129001547590](https://i0.hdslb.com/bfs/album/dfa229c893173a2edcc7860908956f85c1cc2f4d.png)

#### 示例代码

##### Publisher

```java
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
```

##### <a id="ack">Consumer</a>

> 关闭消费者自动ACK并设置消息流控(Qos),最终实现消费快的消费者尽可能多的去消费

```java
package com.rhys.rabbitmq.models.workqueues;

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
        channel.queueDeclare(Publisher.QUEUE_NAME, false, false, false, null);
        //设置消息流控
        channel.basicQos(1);
        //消费消息
        channel.basicConsume(Publisher.QUEUE_NAME, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("consumer1消费到消息:" + new String(body, StandardCharsets.UTF_8));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
        System.out.println("consumer1开始监听队列:" + Publisher.QUEUE_NAME);
        System.in.read();
    }

    @Test
    public void consumer2() throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建队列 参数要与发布消息时统一
        channel.queueDeclare(Publisher.QUEUE_NAME, false, false, false, null);
        //设置消息流控
        channel.basicQos(1);
        //消费消息
        /*
         * 如果需要让性能高的消费者尽可能多的消费，就要关闭自动ACK:false，改为手动ACK
         * */
        channel.basicConsume(Publisher.QUEUE_NAME, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("consumer2消费到消息:" + new String(body, StandardCharsets.UTF_8));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
        System.out.println("consumer2开始监听队列:" + Publisher.QUEUE_NAME);
        System.in.read();
    }
}
```

##### 运行结果

![image-20221129010142624](https://i0.hdslb.com/bfs/album/4eba8ae3daebe21df0c56f226f006795dd911145.png)

### [Publish/Subscribe](https://www.rabbitmq.com/tutorials/tutorial-three-python.html)

> 自定义一个交换机并制定交换机类型为`FanOut`
>
> 让交换机通过`RoutingKey`同时绑定多个队列，再由不同的消费者分别监听每个队列

![image-20221129011044863](https://i0.hdslb.com/bfs/album/b4b9e9bb20f7e0c8967d50db50e0b9722b6d0194.png)

#### 示例代码

##### Publisher

```java
package com.rhys.rabbitmq.models.fanout;

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
    public static final String QUEUE_NAME1 = "fanoutQ1";
    public static final String QUEUE_NAME2 = "fanoutQ2";
    public static final String EXCHANGE_NAME = "fanoutExchange";

    public static void main(String[] args) throws Exception {
        //构建连接对象
        Connection connection = ConnectionUtil.getConnection();
        //构建channel
        Channel channel = connection.createChannel();
        //构建交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        //构建队列（队列名称,队列是否需要持久化,是否设置为排外队列(只能由一个消费者监听),长时间未使用自动删除，其他参数）
        channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
        channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
        //绑定交换机和队列(队列名,交换机名,路由名)，因为使用的是fanout类型交换机，所以routingKey写什么无所谓了，已经直接绑定了
        channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME, "");
        channel.queueBind(QUEUE_NAME2, EXCHANGE_NAME, "");
        //发送消息
        channel.basicPublish(EXCHANGE_NAME, "", null, "pubsub".getBytes(StandardCharsets.UTF_8));
        System.out.println("消息发送成功");
    }
}
```

##### 图形界面展示

> 交换机创建成功

![image-20221129013004309](https://i0.hdslb.com/bfs/album/5872e02be5a7cff8a998b7bb4934993f350ab7c4.png)

> 队列创建成功并成功发送消息

![image-20221129013028528](/Users/Rhys.Ni/Library/Application Support/typora-user-images/image-20221129013028528.png)

> 交换机与队列绑定成功

![image-20221129013155915](https://i0.hdslb.com/bfs/album/e9c61d13474aed673f90dd6d9626c4d4765b5882.png)

##### Consumer

```java
package com.rhys.rabbitmq.models.fanout;

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
```

##### 运行结果

![image-20221129013557204](https://i0.hdslb.com/bfs/album/1bfe1e72b9c469ecee06d4ec18863d3c723d9269.png)

### [Routing](https://www.rabbitmq.com/tutorials/tutorial-four-python.html)

> `Direct`
>
> 将消息根据具体的`RoutingKey`发送到完全匹配的队列中，再由不同的消费者对各自监听的队列中消息进行消费

![image-20221129021024518](https://i0.hdslb.com/bfs/album/3967e137151f35fcca315e095961f2dc9c8d7b54.png)

#### 示例代码

##### publisher

> 在绑定交换机和队列时，需要指定好路由key，同时在发送消息时也制定具体的路由key，只有路由key完全一致时，才会把指定的消息路由到指定的队列中

```java
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
        //绑定交换机和队列(队列名,交换机名,路由名)，因为使用的是fanout类型交换机，所以routingKey写什么无所谓了，已经直接绑定了
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
```

##### consumer

```java
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
```

##### 执行结果

![image-20221129020331813](https://i0.hdslb.com/bfs/album/3506e26c049b8ac1af49b9ebca5f24747973712d.png)

### [Topics](https://www.rabbitmq.com/tutorials/tutorial-five-python.html)

> `Topic`
>
> 根据模式（主题）接收消息

![image-20221129023813228](https://i0.hdslb.com/bfs/album/b9776e004c0745e186618a731563a7cebbd0b49c.png)

#### 示例代码

##### publisher

> 根据*和#号来制定路由规则实现以下效果
>
> - 让两个队列都能接收到第一条消息
> - 让第二条消息只发送到队列二中
> - 让第三条消息发送到队列二中

```java
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
```

##### consumer

```java
package com.rhys.rabbitmq.models.topic;

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
```

##### 运行结果

![image-20221129023355947](https://i0.hdslb.com/bfs/album/ce2cf4f7cb37ff8630bcffcb817159196569faad.png)



### [RPC](https://www.rabbitmq.com/tutorials/tutorial-six-python.html)

> 适用于需要完成两个服务调用并且有一定解耦的场景
>
> - 客户端会发送一个消息到MQ队列
> - 服务端会监听队列的消息拿到具体内容
> - 再将响应交给另一个MQ队列，客户端会监听这个响应队列最终完成一个请求的闭环

![image-20221129035155272](https://i0.hdslb.com/bfs/album/39f16de04cb253e2a6f4bd5b7f91e6adf03a0256.png)

#### 示例代码

##### publisher

```java
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
```

##### consumer

```java
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
```

##### 运行结果

![image-20221129035401858](https://i0.hdslb.com/bfs/album/0f490cb8e7590c1b3f664c947eb469734339a913.png)

### [Publisher Confirms](https://www.rabbitmq.com/tutorials/tutorial-seven-java.html)(保证消息可靠性)

#### 发送途中消息丢失

> - 生产者收到命令将消息发出，随机生产者认为已经将这条消息发送到了交换机中
> - 但是由于网络原因导致消息在发送途中丢失了没有到达交换机

![image-20221219170830990](https://i0.hdslb.com/bfs/album/3be848b1bf5a29a8f6684af362a7f0e72b57d4aa.png)

##### 解决方案

>**保证消息成功发送到了交换机**
>
>利用`Confirm机制`，根据`ConfirmListener`的异步回调，进入`handleAck`方法确定消息发送成功

##### 代码示例

```java
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
```

#### 服务宕机消息丢失

> - 交换机不具备持久化消息的能力
> - 当服务器宕机或MQ重启服务时就会导致没有路由到队列中的消息丢失
> - 但是队列默认是`不具备持久化消息`的能力，需要`手动开启(DeliveryMode)`

![image-20221219171459517](https://i0.hdslb.com/bfs/album/1b23e1ad4aba33c9cdfb49ef6bc300b49dc64206.png)

##### 解决方案1

> **保证消息成功路由到了队列中**
>
> - 利用`return机制` ，确认消息是否路由到了队列
> - 只有当`消息没有路由到队列时`才会`回调return`方法
> - 需要手动将队列改为：QUEUE_NAME+"1111"，使其在我们现有的MQ服务中不存在即可

##### 代码示例1

```java
//return机制 （确认消息是否路由到了队列）
//消息没有路由到队列时会回调return方法
channel.addReturnListener((replyCode, replyText, exchange, routingKey, basicProperties, bytes) 
                          -> System.out.println("消息已送达到交换机，但未路由到指定队列，可做补偿机制..."));
```

##### 解决方案2

> **保证队列支持消息持久化** 
>
> - `DeliveryMode设置消息持久化:（设置为1：不持久化消息 | 设置为2：持久化消息）`

##### 代码示例2

```java
//设置消息持久化
//服务器宕机/MQ服务重启消息依旧会存在队列中
AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
  //2:持久化消息 1:不持久化消息
  .deliveryMode(2)
  .build();

//在发送消息时需要将'mandatory'属性设置为ture,代表开启return机制
//basicPublish(交换机, 队列, mandatory值,属性, 消息内容)
//QUEUE_NAME+"111"，手动调整为不存在的队列，否则无法回调return方法
channel.basicPublish("", QUEUE_NAME, true, properties, msg.getBytes(StandardCharsets.UTF_8));
```

#### 消费者消费失败

![image-20221219171950255](https://i0.hdslb.com/bfs/album/184ea9cb7519320b964a1cd3ac620e4781878bdf.png)

##### 解决方案

> **保证消费者正常消费消息**
>
> - [设置手动ACK](#ack)

```java
//设置消息流控
channel.basicQos(1);
//消费消息
//如果需要让性能高的消费者尽可能多的消费，就要关闭自动ACK:false，改为手动ACK
channel.basicConsume(Publisher.QUEUE_NAME, false, new DefaultConsumer(channel) {
  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    System.out.println("consumer2消费到消息:" + new String(body, StandardCharsets.UTF_8));
    channel.basicAck(envelope.getDeliveryTag(), false);
  }
});
```

#### 上述案例完整代码

```java
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
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, basicProperties, bytes) 
                                  -> System.out.println("消息已送达到交换机，但未路由到指定队列，可做补偿机制..."));

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
```

## SpringBoot集成RabbitMQ

> NEW PROJECT

![image-20221122025339371](https://i0.hdslb.com/bfs/album/b81061c0acfbf940fc720c4d158638e78cc7d246.png)

> 勾选`mq`依赖

![image-20221122025525478](https://i0.hdslb.com/bfs/album/0791c498c6877f7c41450ffd83bc73f208e13167.png)

> 具体依赖如下

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.amqp</groupId>
        <artifactId>spring-rabbit-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 配置RabbitMQ

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    listener:
      simple:
        #手动ACK
        acknowledge-mode: manual
        #每次接收多少条消息
        prefetch: 10
```

### 创建RabbitMQ配置类，声明交换机和队列信息

> - 定义交换机(`Exchange:topicExchange`）
> - 定义队列(`Queue:durable：持久化队列、nonDurable：非持久化队列`）
> - 将交换机与队列通过RoutingKey建立绑定关系(`Binding:bind,to,with`)

```java
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "testExchange";
    public static final String QUEUE = "testQueue";
    public static final String ROUTING_KEY = "*.test.*";

    /**
     * 定义交换机
     *
     * @param
     * @return org.springframework.amqp.core.Exchange
     * @author Rhys.Ni
     * @date 2022/12/13
     */
    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).build();
    }

    /**
     * 定义队列
     *
     * @param
     * @return org.springframework.amqp.core.Queue
     * @author Rhys.Ni
     * @date 2022/12/13
     */
    @Bean
    public Queue queue() {
        //durable：持久化队列
        //nonDurable：非持久化队列
        return QueueBuilder.durable(QUEUE).build();
    }

    /**
     * 将交换机与队列通过RoutingKey建立绑定关系
     *
     * @param exchange
     * @param queue
     * @return org.springframework.amqp.core.Binding
     * @author Rhys.Ni
     * @date 2022/12/13
     */
    @Bean
    public Binding binding(Exchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
```

### 创建生产者

```java
/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/12/13 11:54 PM
 */
@SpringBootTest
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息（不携带属性）
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/12/14
     */
    @Test
    public void convertAndSend() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "RhysNi.test.Ni", "RhysNi RabbitMQ 实战手册");
        System.out.println("消息发送成功");
    }

    /**
     * 发送消息（携带属性）
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2022/12/14
     */
    @Test
    public void convertAndSendWithProps() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "RhysNi.test.Ni", "RhysNi RabbitMQ 实战手册", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setCorrelationId(UUID.randomUUID().toString());
                return message;
            }
        });
        System.out.println("消息发送成功");
    }
}
```

> 发送成功后查看RabbitMQ图形化界查看消息已经成功发送到对应的队列中，下面咱们就创建消费者去监听队列将这条消息消费掉

​	![image-20221214001304299](https://i0.hdslb.com/bfs/album/5d7f30e55b639e60638ae5563bab54ab36faf23c.png)

​	![image-20221214001329748](https://i0.hdslb.com/bfs/album/727636768b891b05b5ae1502aa645661dfb9471a.png)

### 创建消费者监听

> `correlationId`：消息唯一标识

```java
import com.rabbitmq.client.Channel;
import com.rhys.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2022/12/13 11:54 PM
 */
@Component
public class ConsumerListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumer(String msg, Channel channel, Message message) throws IOException {
        System.out.println("msg:" + msg);
        String correlationId = message.getMessageProperties().getCorrelationId();
        System.out.println("correlationId:" + correlationId);
        //手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
```

> 重新启动可以看到消息已经被消费者接受并且消费

![image-20221214015458709](https://i0.hdslb.com/bfs/album/0eebef6dfb8a2b979eddd91cd00f4670e3fa10e5.png)

## Springboot实现消息可靠传输

### Confirm机制

#### 配置

> 配置`application.yml`，开启`confirm机制`

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    listener:
      simple:
        #手动ACK
        acknowledge-mode: manual
        #每次接收多少条消息
        prefetch: 10
    #开启confirm机制
    publisher-confirm-type: correlated
```

#### 代码示例

```java
@SpringBootTest
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Confirms机制
     * @author Rhys.Ni
     * @date 2023/1/5
     * @param
     * @return void
     */
    @Test
    public void convertAndSendConfirm() {
        rabbitTemplate.setConfirmCallback((correlationData, isAck, strCause) -> {
            if (isAck){
                System.out.println("消息成功发送到交换机中");
            }else{
                System.out.println("消息发送到交换机失败,进行补偿操作");
            }
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "RhysNi.test.Ni", "RhysNi RabbitMQ 实战手册（Confirms机制）");
        System.out.println("消息发送成功（Confirms机制）");
    }
}
```

> 运行结果

![image-20230105031243157](https://i0.hdslb.com/bfs/album/90efb1ae9688983b5597cffb0e07f2c16095d2cc.png)

### Return机制

#### 配置

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    listener:
      simple:
        #手动ACK
        acknowledge-mode: manual
        #每次接收多少条消息
        prefetch: 10
    #开启confirm机制
    publisher-confirm-type: correlated
    #开启Return机制
    publisher-returns: true
```

#### 代码示例

> **returnedMessage中可获取到交换机以及路由信息**

```java
@SpringBootTest
public class Publisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Return机制
     * @author Rhys.Ni
     * @date 2023/1/5
     * @param
     * @return void
     */
    @Test
    public void convertAndSendReturn() {
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            String msg = new String(returnedMessage.getMessage().getBody());
            System.out.println("msg:"+msg+"路由到队列失败，进行补偿操作,returnedMessage中可获取到交换机以及路由信息，可重新发送到交换机等。。。");
        });
        //由于RabbitMQConfig中配置的路由规则是 *.test.* 所以这里需要手动将 Rhys.test.Ni 改为 Rhys.test1.Ni 模拟失败操作
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "Rhys.test1.Ni", "RhysNi RabbitMQ 实战手册（Return机制）");
        System.out.println("消息发送成功（Return机制）");
    }
}
```

> 运行结果

![image-20230105033007810](https://i0.hdslb.com/bfs/album/10cfee01ff5a74e5a0bfe7ccca8ac615f1eefe60.png)

> 如果将路由Key改回正确的`Rhys.test.Ni`则不会进入此回调，代表路由到队列成功了

![image-20230105033142643](https://i0.hdslb.com/bfs/album/7a3321beb0d342efaba7ca05ca62edcf98f2ef2e.png)

### 消息持久化

#### 代码示例

> - new MessagePostProcessor()
> - `MessageDeliveryMode.PERSISTENT`(持久化)
> - `MessageDeliveryMode.NON_PERSISTENT` (不持久化)

```java
    /**
     * 消息持久化(new MessagePostProcessor())
     * MessageDeliveryMode.PERSISTENT 持久化
     * MessageDeliveryMode.NON_PERSISTENT 不持久化
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2023/1/5
     */
    @Test
    public void convertAndSendBasicProps() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "Rhys.test.Ni", "RhysNi RabbitMQ 实战手册（消息持久化）", message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        System.out.println("消息发送成功（消息持久化）");
    }
```

## 死信队列（[Dead Lettering](https://www.rabbitmq.com/dlx.html)）

> - 消费者拿到消息之后执行了nack或者reject，并且设置requeue为false，也就是说消费者拒绝消费这条消息，并且不让该条消息重新返回队列被别的消费者消费，则该消息为死信
>
> - 消息在队列中`生存时间(Message TTL)`超时了还没被消费
>   - 可以针对某一个队列设置生存时间
>   - 也可以指定队列中所有消息的生存时间
>
> - 队列已经到了消息的`最大长度(x-max-length)`后，后面再路由过来的消息直接为死信

### 应用场景

> - 基于死信队列在队列消息已满的情况下，消息也不会被静默删除导致消息丢失
> - 可以用于实现电商下单的延迟消费（5分钟付款时间）

### 死信队列实现

> - 当生产者发布了一条消息到负责处理正常消息的交换机
> - 消息成功路由到了正常队列中
> - 如果监听正常队列的消费者拿到消息后执行了`NACK`或`Reject`，并且设置`requeue=false`拒绝消费这条消息同时不让该条消息重新返回队列被别的消费者消费，则该消息为死信
> - 这时这条消息将由正常队列重新路由到`死信交换机`中
> - 再由`死信交换机`根据`路由Key`路由到对应的`死信队列中`
> - 最后由`死信消费者`监听这个`死信队列并拿到消息进行消费`

​	![image-20230109233055293](https://i0.hdslb.com/bfs/album/2e1c95bcdcc9d9bac20833640dfb9a8a6acc6d4e.png)

> 定义死信交换机和死信队列

```java
@Configuration
public class DeadLetteringConfig {
    //正常交换机
    public static final String EXCHANGE = "test-exchange";
    //死信交换机
    public static final String DEAD_EXCHANGE = "dead-test-exchange";


    //正常队列
    public static final String QUEUE = "test-queue";
    //死信队列
    public static final String DEAD_QUEUE = "dead-test-queue";


    //正常路由key
    public static final String ROUTING_KEY = "*.test.#";
    //死信路由key
    public static final String DEAD_ROUTING_KEY = "dead.test.*";


    @Bean
    public Exchange testExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).build();
    }

    /**
     * deadLetterRoutingKey消息变为死信时路由key需要被修改成什么
     * @author Rhys.Ni
     * @date 2023/1/10
     * @param
     * @return org.springframework.amqp.core.Queue
     */
    @Bean
    public Queue testQueue() {
        return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_EXCHANGE).deadLetterRoutingKey("dead.test.ni").build();
    }

    @Bean
    public Binding testBinding(Queue testQueue, Exchange testExchange) {
        return BindingBuilder.bind(testQueue).to(testExchange).with(ROUTING_KEY).noargs();
    }



    @Bean
    public Exchange deadTestExchange() {
        return ExchangeBuilder.topicExchange(DEAD_EXCHANGE).build();
    }

    @Bean
    public Queue deadTestQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    @Bean
    public Binding deadBinding(Queue deadTestQueue, Exchange deadTestExchange) {
        return BindingBuilder.bind(deadTestQueue).to(deadTestExchange).with(DEAD_ROUTING_KEY).noargs();
    }
}
```

> 消费者拿到消息之后执行了nack或者reject，并且设置requeue为false

```java
@Component
public class DeadConsumerListener {

    @RabbitListener(queues = DeadLetteringConfig.QUEUE)
    public void consumer(String msg, Channel channel, Message message) throws IOException {
        System.out.println("正常队列msg:" + msg);
        //tag,requeue
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
      	
      	//tag,multiple,requeue 二选一即可
        //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
    }
}
```

> 针对某一个队列设置生存时间

```java
/**
 * deadLetterRoutingKey消息变为死信时路由key需要被修改成什么
 * @author Rhys.Ni
 * @date 2023/1/10
 * @param
 * @return org.springframework.amqp.core.Queue
 */
@Bean
public Queue testQueue() {
  return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_EXCHANGE).deadLetterRoutingKey("dead.test.ni")
    //3s过后没被消费被转入死信队列
    .ttl(3000).build();
}
```

> 指定队列中所有消息的生存时间

```java
public void convertAndSendExpire() {
  rabbitTemplate.convertAndSend(DeadLetteringConfig.EXCHANGE, "Rhys.test.Ni", "RhysNi RabbitMQ 实战手册（死信测试）", 
                                message -> {
    //3s过后没被消费被转入死信队列
    message.getMessageProperties().setExpiration("3000");
    return message;
  });
  System.out.println("消息发送成功");
}
```

> 队列已经到了消息的`最大长度(x-max-length)`后，后面再路由过来的消息直接为死信

```java
/**
 * deadLetterRoutingKey消息变为死信时路由key需要被修改成什么
 * @author Rhys.Ni
 * @date 2023/1/10
 * @param
 * @return org.springframework.amqp.core.Queue
 */
@Bean
public Queue testQueue() {
  return QueueBuilder.durable(QUEUE).deadLetterExchange(DEAD_EXCHANGE).deadLetterRoutingKey("dead.test.ni")
    //3s过后没被消费被转入死信队列
    // .ttl(3000)
    //设置队列最大长度为1模拟超出队列长度，后续路由过来的消息转死信场景
    .maxLength(1)
    .build();
}
```

### 延迟交换机

> - RabbitMQ只会监听队列最外侧的消息
> - 如果出现最外侧消息生存时间比后面消息都长
> - 就会导致后面的消息要等到最外侧消息生成时间到期进入死信队列后才能去监听后面消息的生存时间
> - **这种情况就可能会导致Msg2的生存时间早就到期，却只能等待10s过后两条消息一起进了死信队列**
>
> **总结：死信队列实现延迟消费时，如果延迟时间比较复杂，比较多，直接使用死信队列时，需要创建大量的队列还对应不同的时间，可以采用延迟交换机来解决这个问题**
>
> - **不过，如果刚好生产者发送了一个延时消息到交换机中，这时服务器宕机或服务重启消息就丢失了**

​	![image-20230110025712023](https://i0.hdslb.com/bfs/album/28025f6294dc2fc74f132d6561f0956d6c484ec9.png)

#### 延迟交换机安装

> - **[点击下载延迟交换机插件(rabbitmq_delayed_message_exchange)](https://objects.githubusercontent.com/github-production-release-asset-2e65be/32327910/b9de3cea-69df-4b49-a647-d2174745e7c0?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIWNJYAX4CSVEH53A%2F20230109%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20230109T192336Z&X-Amz-Expires=300&X-Amz-Signature=c307d082d6727c005d1fbb73afa3f4735b8a67f69bca3c0cdee999398649a1d9&X-Amz-SignedHeaders=host&actor_id=58049956&key_id=0&repo_id=32327910&response-content-disposition=attachment%3B%20filename%3Drabbitmq_delayed_message_exchange-3.11.1.ez&response-content-type=application%2Foctet-stream)**
> - 移动到MQ服务的安装路径下的`plugins`目录

![image-20230110032622610](https://i0.hdslb.com/bfs/album/de28cccb8b1214a2b49dfb2b3a4026fa1e0bef9a.png)

> 移动到`plugins`目录后，跳转到`sbin`目录

![image-20230110032842863](https://i0.hdslb.com/bfs/album/abadc306ad69ff2d7cb77e843654a53d0748bfcb.png)

> 使用`rabbitmq-plugins`运行插件

```shell
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

> 没有运行延迟交换机插件之前我们想创建一个交换机类型只有以下图中四种

![image-20230110033103899](https://i0.hdslb.com/bfs/album/f20e9086488ca296ad0efdeee547830facef4e26.png)



> 当我们开启延迟交换机插件后发现可选择的交换机类型中多了一个`x-delayed-message`类型

![image-20230110033304367](https://i0.hdslb.com/bfs/album/8365b714f3c1ca409391f6f74f22b30485810e64.png)

#### 代码案例

```java
@Configuration
public class DelayedConfig {
    //正常交换机
    public static final String DELAYED_EXCHANGE = "delayed-exchange";
    //正常队列
    public static final String DELAYED_QUEUE = "delayed-queue";
    //正常路由key
    public static final String DELAYED_ROUTING_KEY = "delayed.test.#";

    @Bean
    public Exchange delayedExchange() {
        Map<String, Object> args = new HashMap<>(1);
        //指定延迟交换机类型
        args.put("x-delayed-type", "topic");

        Exchange exchange = new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
        return exchange;
    }

    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable(DELAYED_QUEUE).build();
    }

    @Bean
    public Binding delayedBinding(Queue delayedQueue, Exchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }
}
```

> 测试发送延时消息

```java
@SpringBootTest
public class DelayedPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void convertAndSend() {
        rabbitTemplate.convertAndSend(DelayedConfig.DELAYED_EXCHANGE, "delayed.test.Ni", 
                                      "RhysNi RabbitMQ 实战手册（延迟交换机测试）");
        System.out.println("消息发送成功");
    }

    @Test
    public void convertAndSendExpire1() {
        rabbitTemplate.convertAndSend(DelayedConfig.DELAYED_EXCHANGE, "delayed.test.Ni", 
                                      "RhysNi RabbitMQ 实战手册（延迟交换机测试）", message -> {
            //3s过后没被消费被转入死信队列
            message.getMessageProperties().setDelay(30000);
            return message;
        });
        System.out.println("消息发送成功");
    }

    @Test
    public void convertAndSendExpire2() {
        rabbitTemplate.convertAndSend(DelayedConfig.DELAYED_EXCHANGE, "delayed.test.Ni",
                                      "RhysNi RabbitMQ 实战手册（延迟交换机测试）", message -> {
            //3s过后没被消费被转入死信队列
            message.getMessageProperties().setDelay(3000);
            return message;
        });
        System.out.println("消息发送成功");
    }
}
```

## 高可用集群

### RabbitMQ镜像模式

> 提供高可用服务的同时提升MQ效率
>
> - 如果没有Nginx做负载均衡,则有可能会使请求不均匀的打到MQ服务上，
> - 请求量过大的某一个服务处理消息就会非常繁忙，
> - 而请求量少的服务相对空闲时长较多，导致消息消费速度下降

​	![image-20230112004905538](https://i0.hdslb.com/bfs/album/3fbff780f5161f55ae5af4726b0b1eb0660a9d7f.png)

#### Docker安装最新版RabbitMQ

```shell
docker pull rabbitmq:latest
```

> 运行容器

```shell
docker run -d --hostname localhost --name rabbitmq -p 15672:15672 -p 5673:5672 rabbitmq
```

> 开启管理界面

```shell
docker exec -it 容器id /bin/bash
```

```shell
rabbitmq-plugins enable rabbitmq_management
```

#### 配置Docker yml文件

> 首先分别进入每台机器,创建对应目录文件夹`/usr/local/docker/rabbitmq-cluster_docker`

```shell
sudo mkdir -p  /usr/local/docker/rabbitmq-cluster_docker
```

> 第一台MQ配置
>
> - 在`/usr/local/docker/rabbitmq-cluster_docker`目录下创建`docker-compose.yml`文件
> - 最后将docker脚本贴进去保存退出

```yaml
version: '3.1'
services:
  rabbitmq1:
    image: rabbitmq:3.11.0-management-alpine
    container_name: rabbitmq
    hostname: rabbitmq1
    extra_hosts:
      - "rabbitmq1:172.19.105.54"
      - "rabbitmq2:101.133.157.40"
    environment: 
      - RABBITMQ_ERLANG_COOKIE=RhysNi
    ports:
      - 5672:5672
      - 15672:15672
      - 4369:4369
      - 25672:25672
```

> 第二台MQ配置同上，将配置内容替换成以下内容即可

```yaml
version: '3.1'
services:
  rabbitmq1:
    image: rabbitmq:3.11.0-management-alpine
    container_name: rabbitmq
    hostname: rabbitmq2
    extra_hosts:
      - "rabbitmq1:172.19.105.54"
      - "rabbitmq2:101.133.157.40"
    environment: 
      - RABBITMQ_ERLANG_COOKIE=RhysNi
    ports:
      - 5672:5672
      - 15672:15672
      - 4369:4369
      - 25672:25672
```

> 最后分别在`/usr/local/docker/rabbitmq-cluster_docker`目录下执行以下命令启动容器

```shell
docker-compose up -d
```

> 特殊提示📢：如果`docker-compose`	命令报错`-bash: docker-compose: command not found`
>
> [command not found解决办法](https://blog.csdn.net/qq_35663625/article/details/107411857)
>
> 最后等待跑完即可

![image-20230112023214774](https://i0.hdslb.com/bfs/album/7284dedcbbefee0a58d701cf503b2e448e2aa3de.png)
