import RabbitMQ.MQSender;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.json.simple.JSONObject;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hillold
 */
public class BootManager {
    public void startBoot(String IP) throws IOException, TimeoutException, InterruptedException
    {
        // JSONObject jst = new JSONObject();
        // jst.put("type","vmrequest");
        // jst.put("operation", "spawn");
        MQSender mq = new MQSender("LBTOVMM",IP);
        //  mq.send(jst.toJSONString());
        mq.send("REQ_SPWN SPAWN");
        this.waitTillSpawn();
    }
    
    private void waitTillSpawn() throws InterruptedException
    {
        System.out.println("Booting");
        LoadInformation li = new GlobalStorage();
        int i=0;
        while(li.getVMLoad().size()==0)
        {
            Thread.sleep(1000);
            System.out.print(".");
            i++;
            if(i%4==0)
                System.err.print("\b\b\b");
        }
        li.showCurrentInfo();
    }
    
}
