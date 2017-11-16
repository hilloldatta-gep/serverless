import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author hillold
 */
public class SystemInfoReceiver extends DefaultConsumer{
    private final LoadInformation li = new GlobalStorage("RoundRobin");
    private final JSONParser parser = new JSONParser();
    public SystemInfoReceiver(Channel channel) {
        super(channel);
        //System.out.println("SysIR");
    }
    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
       String message = new String(body, "UTF-8");
       Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.INFO,"NEW SERVICE ADDED: "+message);
       Object obj=null;
        try {
            obj = parser.parse(message);
        } catch (ParseException ex) {
            Logger.getLogger(SystemInfoReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
       JSONObject jsonObject = (JSONObject) obj;
       li.updateServiceVMInfo(jsonObject.get("svcname").toString(),jsonObject.get("vmname").toString());
    }
}
