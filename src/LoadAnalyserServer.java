import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import RabbitMQ.MQSender;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hillold
 */
public class LoadAnalyserServer implements Runnable{
    private LoadInformation loadInformation;
//	private final String Qname = "LBtoSLM";
        private final String Qname = "LBTOVMM";
	private final int thetaOverload = 50;
	private final int thetaUnderload = 50;
	private final int timeout = 10;
        private Map<String,Integer> vmloadedtime = new HashMap<String,Integer>();
        Properties prop = new Properties();
        
        
	public LoadAnalyserServer(LoadInformation loadInformation1) {
		loadInformation = loadInformation1;
            try {
                prop.load(new FileInputStream("sys.properties"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoadAnalyserService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LoadAnalyserService.class.getName()).log(Level.SEVERE, null, ex);
            }
	}

	public LoadAnalyserServer() {
		loadInformation = new GlobalStorage();
            try {
                prop.load(new FileInputStream("sys.properties"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoadAnalyserService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LoadAnalyserService.class.getName()).log(Level.SEVERE, null, ex);
            }
		
	}
	public void sendSignal() throws IOException {
		Algorithm algorithm = new AlgorithmImpl(loadInformation);

		HashMap<String, Integer> loadHashMap = algorithm.getLoadServer();
		try {
			loadAnalyse(loadHashMap);
		} catch (TimeoutException ex) {
			Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


	private void loadAnalyse(HashMap<String, Integer> loadHashMap) throws IOException, TimeoutException {
		for(String vm : loadHashMap.keySet())
		{
			if(loadHashMap.get(vm)>thetaOverload)
			{
				Logger.getLogger(Manager.class.getName()).log(Level.WARNING,"VM "+vm+" OVERLOADED");
				takeActionForOverLoad(vm);
                            try {
                                Thread.sleep(20000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(LoadAnalyserServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
				
			}
			else
			{
				Logger.getLogger(Manager.class.getName()).log(Level.INFO,"VMLOAD STATUS: NORMAL");
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	private void takeActionForOverLoad(String vm) throws IOException, TimeoutException {
            
            if(!vmloadedtime.containsKey(vm))
                vmloadedtime.put(vm,1);
            else
                vmloadedtime.put(vm,vmloadedtime.get(vm)+1);
            
		if(!(null==vm) && vmloadedtime.get(vm)>timeout)
		{
                    // JSONObject jsonObject = new JSONObject();
                    // jsonObject.put("request","spwan");
                    // String message = jsonObject.toJSONString();
                    MQSender resp = new MQSender(Qname, prop.getProperty("MQSERVER_ADDRESS"));
                    resp.send("REQ-OVERLOAD-SPWN SPAWN");
                    vmloadedtime.put(vm,0);
		}
		else
		{
                    Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null,"NUll in Vm");
		}
	}

	@Override
	public void run() {
		while(true){
			try {
	                    sendSignal();
	                    try {
	                        Thread.sleep(5000);
	                    } catch (InterruptedException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                        Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null, e);
	                    }
	                } catch (Exception ex) {
				Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null, ex);
			}
			}
		
	}
	
}
