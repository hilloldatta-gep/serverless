import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hillold
 */
public class Manager {
    private final RequestManager rqm = new RequestManager();
    private final JSONParser parser = new JSONParser();
    private int globalCounter=0;
    private LoadInformation li=null;
    private Map<String,Integer> mp=null;
    //private  LogHandle lg;
    public Manager()
    {
        li = new GlobalStorage("normal");
        //System.out.println(li.getServiceLoad().toString());
        mp = new HashMap<>();
    }
    public void handleRequest(String message) throws ParseException
    {
        //System.out.println("Entered Manager"+getNextVM(getServiceName(message),"normal"));
        //lg = new LogHandle("INFO",false, LoadBalancer.class.getName(), new Object(){}.getClass().getEnclosingMethod().getName()+" - "
        //        + ""+"Service Request Received: "+getServiceName(message),"DB");
        //lg.registerLog("localhost");
        if(getNextVM(getServiceName(message),"normal")==null)
        {
            Logger.getLogger(Manager.class.getName()).log(Level.WARNING,"SERVICE NOT RUNNING: REQUESTING NEW INSTANCE..");
            Thread t = new Thread(new NewRequestHandler(getServiceName(message),message));
            t.start();
        }
        else if("request".equals(getType(message))){
            Logger.getLogger(Manager.class.getName()).log(Level.INFO, "REQUEST RECEIVED AND FORWARDED");
            rqm.handleRequest(message,getQueueName(message),getServiceName(message),getNextVM(getServiceName(message),"normal"));
        }
        else
            try {
//                Logger.getLogger(Manager.class.getName()).log(Level.INFO, "RESPONSE : "+message);
                rqm.handleResponse(message, getServiceName(message),getQueueName(message));
        } catch (TimeoutException ex) {
            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private String getType(String message) throws ParseException
    {
       Object obj = parser.parse(message);
       JSONObject jsonObject = (JSONObject) obj; 
       return jsonObject.get("type").toString();    
    }
    
    private String getServiceName(String message) throws ParseException
    {
       Object obj = parser.parse(message);
       JSONObject jsonObject = (JSONObject) obj; 
       return jsonObject.get("service").toString();  
    }
    
    private String getQueueName(String message) throws ParseException
    {
       Object obj = parser.parse(message);
       JSONObject jsonObject = (JSONObject) obj; 
       return jsonObject.get("qname").toString(); 
    }
    
    private String getNextVM()
    {
        ArrayList<Pair<String,Integer>> mp = li.getCPULoad();
        int size = mp.size();
        String vm= mp.get(globalCounter).getKey();
        this.globalCounter = (this.globalCounter+1)%size; 
        return vm;
    }
    
    private String getNextVM(String service,String type)
    {
        if("RoundRobin".equals(type))
        {
            if(!mp.containsKey(service))
                mp.put(service,0);
            ArrayList<String> vms = li.getAllVmsList(service);
//            Logger.getLogger(Manager.class.getName()).log(Level.FINE,vms.toString());
            if(vms==null) return null;
            Integer t = mp.get(service);
            mp.put(service, (t+1)%vms.size());
            return vms.get(t);
        }
        else
        {
            return li.getMinLoadedNodeForService(service);
        }
    }
}
