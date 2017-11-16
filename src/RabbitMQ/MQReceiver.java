/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RabbitMQ;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author hillold
 */
public class MQReceiver {
    private  Channel channel;
    private  Connection connection;
    private  Consumer consumer; 
    private String Q;
    private final ConnectionFactory factory = new ConnectionFactory();
    public MQReceiver(String QUEUE_NAME, String address) throws IOException, TimeoutException
    {
        factory.setHost(address);
        Q=QUEUE_NAME;
        //System.out.println(Q);
    }
    
    public Channel getChannel() throws IOException, TimeoutException
    {
      connection = factory.newConnection();
      channel = connection.createChannel();
      channel.queueDeclare(Q, false, false, false, null);
      return channel;  
    }
    
    public void setConsumer(Consumer m)
    {
       consumer = m; 
    }
    
    public String getQueueName()
    {
     return Q;   
    }
    public void startListening() throws IOException
    {
        channel.basicConsume(Q, true, consumer);
    }
    public String getMessage() throws IOException, InterruptedException, TimeoutException
    {
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(Q, false, false, false, null);
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        channel.basicConsume(Q,true, new DefaultConsumer(channel) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
           response.offer(new String(body, "UTF-8"));
           //channel.basicCancel(consumerTag);
            try {
                channel.close();
                connection.close();
            } catch (TimeoutException ex) {
                Logger.getLogger(MQReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(Thread.currentThread().getId());
        }
        });
        //this.terminate();
        return response.take(); 
    }
}
