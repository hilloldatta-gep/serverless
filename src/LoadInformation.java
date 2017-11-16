import java.util.ArrayList;
import java.util.Map;
import javafx.util.Pair;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hillold
 */
public interface LoadInformation {
    public ArrayList < Pair<String,Integer> > getCPULoad();
    public Map getVMLoad();
    public void updateServiceVMInfo(String service, String newVM);
    public Map<String,Integer> getAllVMs(String service);
    public ArrayList<String> getAllVmsList(String service);
    public void updateServiceLoad(Map<Pair<String,String>,Integer> mp);
    public Map<Pair<String,String>,Integer> getServiceLoad();
    public String getMinLoadedNode();
    public String getMinLoadedNodeForService(String service);
    public void updateServiceVmLoad(Map<String,Map<String,Integer> > mp);
    public Map<String, Map<String, Integer>> getServiceOnVMLoad();
    public void increaseLoad(String vm);
    public void updateCPULoad(Map<String,Integer> mp);
    public void addCpu(String vm);
    public void addCpuLoad(String vm, Integer load);
    public void showCurrentInfo();
}
