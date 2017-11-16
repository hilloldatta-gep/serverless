import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseExce

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hillold
 */
public class RequestManager {
    private JSONParser prsr = new JSONParser();
    private ServiceRequestSender srs;

    RequestManager(){
        try {   
            srs  = new ServiceRequestSender();
        } catch (IOException ex) {
            Logger.getLogger(RequestManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(RequestManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void handleRequest(String m, String FromQ, String ToSvc, String VM)
    {
        try {
            
//            JSONObject jst = (JSONObject)prsr.parse(m);
//            jst.put("to", VM+"@"+ToSvc);
            srs.sendRequest(m,VM+"@"+ToSvc);
        } catch (IOException ex) {
            Logger.getLogger(RequestManager.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void handleResponse(String m, String FromQ, String ToQ) throws TimeoutException
    {
        try {
            
            srs.sendResponse(ToQ,m);
        } catch (IOException ex) {
            Logger.getLogger(RequestManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
