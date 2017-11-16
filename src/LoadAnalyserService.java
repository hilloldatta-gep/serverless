import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;


import RabbitMQ.MQSender;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class LoadAnalyserService implements Runnable {
    private LoadInformation loadInformation;
	private final String Qname = "LBtoSCLM";
        Properties prop = new Properties();
        private final Integer thetaOverload = 8;
	private final Integer thetaUnderload = 2;
	private final int thetaVMLoad = 20;
        private final int timeout =10;

        
	public LoadAnalyserService(LoadInformation loadInformation1) {
		loadInformation = loadInformation1;
            try {
                prop.load(new FileInputStream("sys.properties"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoadAnalyserService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LoadAnalyserService.class.getName()).log(Level.SEVERE, null, ex);
            }
	}

	public LoadAnalyserService() {
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

		HashMap<String, Map<String, Integer>> loadHashMap = algorithm.getLoadService();
		try {
			loadAnalyse(loadHashMap);
		} catch (TimeoutException ex) {
			Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @param loadHashMap
	 */
	private void loadAnalyse(HashMap<String, Map<String, Integer>> loadHashMap) throws IOException, TimeoutException {
                //System.out.println(""+loadHashMap);
		for (String key : loadHashMap.keySet()) {
			for (String key1 : loadHashMap.get(key).keySet()) {
				if (loadHashMap.get(key).get(key1) > thetaOverload)
					takeActionForOverLoad(key, key1);
				// else if(loadHashMap.get(key).get(key1) > thetaOverload)
				// takeActionForUnderLoad(key,key1);
				else
                                    Logger.getLogger(Manager.class.getName()).log(Level.INFO, "LOAD STATUS: NORMAL");
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void takeActionForOverLoad(String service, String vm) throws IOException, TimeoutException {
		// TODO Auto-generated method stub
		String minloadVm = loadInformation.getMinLoadedNode();
		Map<String, Integer> allvms = loadInformation.getAllVMs(service);
                Logger.getLogger(Manager.class.getName()).log(Level.WARNING, "LOAD STATUS: OVERLOADED");
		if (!(null == minloadVm) && !allvms.containsKey(minloadVm)) {
			Logger.getLogger(Manager.class.getName()).log(Level.WARNING,"SERVICE SPAWNING REQUEST: "+service+" VM: "+minloadVm);
			JSONObject jSONObject = new JSONObject();
			jSONObject.put("msg_type", "spawn");
			jSONObject.put("ip", minloadVm);
			jSONObject.put("svc_name", service);
			String message = jSONObject.toJSONString();
			MQSender resp = new MQSender(Qname,prop.getProperty("MQSERVER_ADDRESS"));
			resp.send(message);
			if (Integer.parseInt(loadInformation.getVMLoad().get(minloadVm).toString()) > thetaVMLoad) {
//				Logger.getLogger(Manager.class.getName()).log(Level.SEVERE,"SPAWN REQ SENT");
			}
			/* THIS LINE HAS TO BE REMOVED IN ORIGINAL IMPLEMENTATION */
			loadInformation.increaseLoad(minloadVm);
		}

	}

	private void takeActionForUnderLoad(String service, String vm) {
		System.out.println("KILL THIS PROCESS " + service + " ON THIS VM " + vm);

	}

	@Override
	public void run() {
		while (true) {
			try {
				sendSignal();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null, e);
				}
			} catch (IOException ex) {
				Logger.getLogger(LoadAnalyser.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
    
}
