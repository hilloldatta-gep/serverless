/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package RabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author hillold
 */
public class MQSender {
    private String QUEUE_NAME=null;
    private final Channel channel;
    private final Connection connection;
    
    public MQSender(String QUE, String address) throws IOException, TimeoutException
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(address);
        connection = factory.newConnection();
        channel = connection.createChannel();
        QUEUE_NAME=QUE;
        channel.queueDeclare(QUE, false, false, false, null);
    }
    
    public MQSender(String address) throws IOException, TimeoutException
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(address);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }
    
    public void send(String message) throws UnsupportedEncodingException, IOException
    {    
        
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));    
    }
    public String getQueueName()
    {
        return QUEUE_NAME;
    }
    public void terminate() throws IOException, TimeoutException
    {
        channel.close();
        connection.close(); 
    }
    
    public String call(String message, String rplyq) throws IOException, InterruptedException
    {
        String replyQueueName = channel.queueDeclare(rplyq,false,false,false,null).getQueue();
        String corrId = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties
            .Builder()
            .correlationId(corrId)
            .replyTo(replyQueueName)
             .build();
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        channel.basicPublish("", QUEUE_NAME, props, message.getBytes("UTF-8"));

        channel.basicConsume(replyQueueName, false, new DefaultConsumer(channel) {
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (properties.getCorrelationId().equals(corrId)) {
          response.offer(new String(body, "UTF-8"));
            }
        }
        });

        return response.take();
    }
}
