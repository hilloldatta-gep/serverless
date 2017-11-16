/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hillold
 */

import RabbitMQ.MQReceiver;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import logpkg.LogHandle;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author diptam
 */
public class LoadBalancer {
    public static void main(String args[]) throws IOException, TimeoutException, InterruptedException
    {
        
        Properties prop = new Properties();
        prop.load(new FileInputStream("sys.properties"));
        
        MQReceiver m1 = new MQReceiver("ROUTER",prop.getProperty("MQSERVER_ADDRESS"));
        MQReceiver m2 = new MQReceiver("SYS_INFO",prop.getProperty("MQSERVER_ADDRESS"));
        MQReceiver m3 = new MQReceiver("STAT_QUEUE",prop.getProperty("MQSERVER_ADDRESS"));
        
        Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.INFO,"MODULES ARE LOADED");
        
        m1.setConsumer(new ServiceRequestListener(m1.getChannel()));
        m2.setConsumer(new SystemInfoReceiver(m2.getChannel()));
        m3.setConsumer(new StatisticsInfoReceiver(m3.getChannel()));
        
        Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.INFO,"COMPONENTS ARE SET");
 
        m1.startListening();
        m2.startListening();
        m3.startListening();
        
        BootManager bt = new BootManager();
        bt.startBoot(prop.getProperty("MQSERVER_ADDRESS"));
        
        LoadInformation loadStatistics = new GlobalStorage();
        LoadAnalyser loadAnalyser = new LoadAnalyser(loadStatistics);
        Thread loadAnalyserThread = new Thread(loadAnalyser);
	loadAnalyserThread.start();
      
    }
}