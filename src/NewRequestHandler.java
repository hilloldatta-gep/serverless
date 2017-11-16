

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import RabbitMQ.MQSender;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author hillold
 */
public class NewRequestHandler implements Runnable{
    LoadInformation li = new GlobalStorage("RoundRobin");
    String request=null;
    String message=null;
    Properties prop = new Properties();
        
    NewRequestHandler(String req,String msg)
    {
        this.request=req;
        message=msg;
        try {
            prop.load(new FileInputStream("sys.properties"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NewRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleNewRequest() throws IOException, TimeoutException
    {
        String vm = li.getMinLoadedNode();
        MQSender mq = new MQSender("LBtoSCLM",prop.getProperty("MQSERVER_ADDRESS"));
        MQSender sq = new MQSender(vm+"@"+this.request,prop.getProperty("MQSERVER_ADDRESS"));
        JSONObject onj = new JSONObject();
        onj.put("msg_type", "spawn");
        onj.put("ip", vm);
        onj.put("svc_name", request);
        mq.send(onj.toJSONString());
        sq.send(message);
        li.updateServiceVMInfo(this.request, vm);
        li.increaseLoad(vm);
    }

    @Override
    public void run() {
        try {
            handleNewRequest();
        } catch (IOException ex) {
            Logger.getLogger(NewRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(NewRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
