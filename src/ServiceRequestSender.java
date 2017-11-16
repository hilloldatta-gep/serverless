/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import RabbitMQ.MQSender;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author hillold
 */
public class ServiceRequestSender {
    private Map<String,MQSender> mqlist;
    Properties prop = new Properties();
        
    public ServiceRequestSender() throws IOException, TimeoutException {
        prop.load(new FileInputStream("sys.properties"));
        mqlist = new HashMap();
    }
    public void sendRequest(String m,String Quename) throws IOException
    {
        try {
            
            this.getMQSender(Quename).send(m);
//            Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.FINE,"QUEUE NAME: "+getMQSender(Quename).getQueueName());
        } catch (TimeoutException ex) {
            Logger.getLogger(ServiceRequestSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private MQSender getMQSender(String qname) throws IOException, TimeoutException
    {
        if (!mqlist.containsKey(qname))
            mqlist.put(qname, new MQSender(qname,prop.getProperty("MQSERVER_ADDRESS")));
        return mqlist.get(qname);
    }
    
    public void sendResponse(String Qname, String message) throws IOException, TimeoutException
    {
        MQSender resp = new MQSender(Qname,prop.getProperty("MQSERVER_ADDRESS"));
        resp.send(message);
        resp.terminate();
    }
}
