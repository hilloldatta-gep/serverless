import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ServiceRequestListener {
    private Manager m;
	private final JSONParser parser = new JSONParser();
	public ServiceRequestListener(Channel channel) {
		super(channel);
		m = new Manager();
		//System.out.println("SRL");
	}

	@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		throws IOException {
			String message = new String(body, "UTF-8");
			Object obj=null;
			String replyqueue = properties.getReplyTo();
			try {
				obj= parser.parse(message);
			} catch (ParseException ex) {

				Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.SEVERE,
						null, ex);
			}

			JSONObject jst = (JSONObject)obj;
			System.out.println(jst.toJSONString());
			if("request".equals(jst.get("type")))
			{
				try {
					obj=parser.parse(message);
				} catch (ParseException ex) {
					Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.SEVERE, null, ex);
				}
				jst.put("qname",replyqueue);
				//            jst.put("from", "ROUTER");
			}
			//System.out.println(message);
			try {
				m.handleRequest(jst.toJSONString());
			} catch (ParseException ex) {
				Logger.getLogger(ServiceRequestListener.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
    
}
