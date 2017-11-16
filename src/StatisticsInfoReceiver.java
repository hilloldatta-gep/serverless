import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import logpkg.LogHandle;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author hillold
 */
public class StatisticsInfoReceiver extends DefaultConsumer {
    private final LoadInformation li = new GlobalStorage("RoundRobin");
    private final JSONParser parser = new JSONParser();
    //private LogHandle lg=null;
    public StatisticsInfoReceiver(Channel channel) {
        super(channel);
        //System.out.println("SIR");
    }
    
    
        @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
       String message = new String(body, "UTF-8");
       
       Object obj=null;
        try {
            obj = parser.parse(message);
        } catch (ParseException ex) {
//            lg = new LogHandle("ERROR",false, LoadBalancer.class.getName(), new Object(){}.getClass().getEnclosingMethod().getName()+" - "
//                + ""+"Statistics Data Parsing, Not Well Defined Format","DB");
//        lg.registerLog("localhost");
        }
       JSONObject jsonObject = (JSONObject) obj;
//       Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.FINE,"STAT: "+obj.toString());
       if("queuestats".equals(jsonObject.get("type").toString()))
           try {
               //li.updateServiceLoad(this.getServiceLoad(message));
               li.updateServiceVmLoad(this.getServiceVmLoad(message));
       } catch (ParseException ex) {
           Logger.getLogger(StatisticsInfoReceiver.class.getName()).log(Level.SEVERE, null, ex);
       }
       else if("vmstats".equals(jsonObject.get("type").toString()))
           try {
               //li.updateServiceLoad(this.getServiceLoad(message));
               li.addCpuLoad(this.getVmFromReport(message),this.getLoadFromReport(message));
       } catch (ParseException ex) {
           Logger.getLogger(StatisticsInfoReceiver.class.getName()).log(Level.SEVERE, null, ex);
       }
       else
       {
           Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.SEVERE, "BAD MESSAGE FORMAT ERROR");
       }
       
    }
    
    private Map<Pair<String,String>, Integer> getServiceLoad(String message) throws ParseException
    {
        Object obj = parser.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject temp;
        JSONArray msg = (JSONArray) jsonObject.get("loads");
        String service,vm;
        Integer len;
        Map<Pair<String,String>, Integer> ret = new HashMap();
        Iterator<JSONObject> iterator = msg.iterator();
        
        while(iterator.hasNext())
        {
           temp = iterator.next();
           service = temp.get("service").toString();
           vm = temp.get("vm").toString();
           len = new Integer(temp.get("length").toString());
           ret.put(new Pair<String,String>(service,vm), len);
        }
        //System.out.println(ret);
        return ret;
    }
    
    private Map<String,Integer> getVmLoad(String report) throws ParseException
    {
        Map<String,Integer> ret = new HashMap();
        Object obj = parser.parse(report);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject temp;
        JSONArray msg = (JSONArray) jsonObject.get("loads");
        String service,vm;
        Integer len;
        Iterator<JSONObject> iterator = msg.iterator();
        
        while(iterator.hasNext())
        {
           temp = iterator.next();
           vm = temp.get("vm").toString();
           len = new Integer(temp.get("load").toString());  
           ret.put(vm, len);
        }
        return ret;
    }
    
    private String getVmFromReport(String report) throws ParseException
    {
        Object obj = parser.parse(report);
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject.get("vm").toString();
    }
    
    private Integer getLoadFromReport(String report) throws ParseException
    {
        Object obj = parser.parse(report);
        JSONObject jsonObject = (JSONObject) obj;
        return Integer.parseInt(jsonObject.get("load").toString());
    }
    
    private Map<String,Map<String,Integer> > getServiceVmLoad(String message) throws ParseException
    {
        Object obj = parser.parse(message);
        JSONObject jsonObject = (JSONObject) obj;
        JSONObject temp;
        JSONArray msg = (JSONArray) jsonObject.get("loads");
        String service,vm;
        Integer len;
        Iterator<JSONObject> iterator = msg.iterator();
        Map<String, Map<String,Integer> > ret = new HashMap();
        
        while(iterator.hasNext())
        {
           temp = iterator.next();
           service = temp.get("service").toString();
           vm = temp.get("vm").toString();
           len = new Integer(temp.get("length").toString());  
           if(ret.containsKey(service))
           {
               ret.get(service).put(vm, len);
           }
           else
           {
               Map<String,Integer> mt = new HashMap();
               mt.put(vm, len);
               //System.out.println(mt);
               ret.put(service, mt);
           }
        }
        return ret;
    }
}
