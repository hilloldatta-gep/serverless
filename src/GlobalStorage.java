import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class GlobalStorage implements LoadInformation  {

    /* map to store VM and Corresponding CPU load*/
    private static Map<String,Integer> cpuload = new HashMap();
    
    /* depricated */
    private static ArrayList < Pair<String,Integer> > rrcpu = new ArrayList();
    
    /* List of Vms running a service : Each key is a service, each list corresponding to a service represents all Vms   */
    private static Map<String , ArrayList<String> > service_vm = new HashMap();
    
    /* depricated */
    private static Map<Pair<String,String>, Integer > service_load = new HashMap();
    
    /* Stores Number of lagging Job in a queue running in  a VM*/
    private static Map<String,Map<String,Integer> > service_load_new = new HashMap();
    
    private Properties prop;
    private InputStream input;
    
    GlobalStorage()
    {
            prop = new Properties();
        try {
            input = new FileInputStream("config.properties");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GlobalStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            prop.load(input);
        } catch (IOException ex) {
            Logger.getLogger(GlobalStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        Set<Object> vms= prop.keySet();
        if(vms!=null)
        {
            for(Object vm : vms)
            {
                cpuload.put(vm.toString(),1);
            }
        }
    }
    
    GlobalStorage(String rr)
    {
        prop = new Properties();
        try {
            input = new FileInputStream("config.properties");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GlobalStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            prop.load(input);
        } catch (IOException ex) {
            Logger.getLogger(GlobalStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        Set<Object> vms= prop.keySet();
        if(vms!=null)
        {
            for(Object vm : vms)
            {
                cpuload.put(vm.toString(),1);
            }
        }
        //this.showCurrentInfo();
    }
    
    @Override
    public void showCurrentInfo()
    {
        System.out.println("\n\n========VM LOAD========");
        System.out.println(cpuload);
        System.out.println("========SERVICE LOAD========");
        System.out.println(service_load_new);
        System.out.println("========SERVICE VM MAP========");
        System.out.println(service_vm);
        System.out.println("\n\n");
    }
    
    @Override
    synchronized public ArrayList <Pair<String,Integer>> getCPULoad() {
        return rrcpu;
    }

    @Override
    synchronized public Map<String,Integer> getVMLoad() {
        return new HashMap(cpuload);
    }

    @Override
    synchronized public void updateServiceVMInfo(String service, String newVM) {
        if(!service_vm.containsKey(service))
            service_vm.put(service, new ArrayList<>());
        if(service_vm.get(service).indexOf(newVM)<0)
            service_vm.get(service).add(newVM);
        if(service_load_new.containsKey(service))
        {
            service_load_new.get(service).put(newVM,1);
        }
        else
        {
            Map<String,Integer> temp = new HashMap();
            temp.put(newVM,1);
            service_load_new.put(service,temp);
        }
        this.showCurrentInfo();
    }

    @Override
    synchronized public Map<String, Integer> getAllVMs(String service) {
        ArrayList<String> str= service_vm.get(service);
        if(str==null)
            return null;
        Map<String,Integer> mp = new HashMap<>();
        for (String s : str){
            mp.put(s,cpuload.get(s));
        }
        return mp;
    }

    @Override
    public synchronized ArrayList<String> getAllVmsList(String service) {
        return service_vm.get(service);
    }

    @Override
    public synchronized void updateServiceLoad(Map<Pair<String, String>, Integer> mp) {
        for (Pair<String,String> key : mp.keySet() )
        {
            if(!service_load.containsKey(key))
                service_load.put(key,1);
            service_load.put(key,mp.get(key));
        }        
//        for (Pair<String,String> s : service_load.keySet())
//        {
//            System.out.println(s.getKey() + " " + s.getValue() + " " +service_load.get(s));
//        }
        this.showCurrentInfo();
    }
    
    /*THIS IS UPDATED FUNCTION WITH NEW DATA STRUCTURE*/
    @Override
    public synchronized void updateServiceVmLoad(Map<String,Map<String,Integer> > mp)
    {
        for(String service : mp.keySet())
        {
            Map<String,Integer> temp= new HashMap();
            for(String vm: mp.get(service).keySet())
            {
                //temp.put(vm, mp.get(service).get(vm));
                if(service_load_new.containsKey(service))
                {
                    service_load_new.get(service).put(vm,mp.get(service).get(vm));
                }
                else
                {
                   temp.put(vm, mp.get(service).get(vm)); 
                   service_load_new.put(service, temp);
                }
                    
            }
            
            //service_load_new.put(service, temp);
        }
        this.showCurrentInfo();
    }

    @Override
    public synchronized Map<Pair<String, String>, Integer> getServiceLoad() {
        return new HashMap(service_load);
    }

    @Override
    public synchronized String getMinLoadedNode() {
        int min = Integer.MAX_VALUE;
        String vm=null;
        for(String s: cpuload.keySet())
        {
            int t = cpuload.get(s);
            if(min>t)
            {
                min=t;
                vm=s;
            }
        }
        return vm;
    }

    @Override
    public synchronized String getMinLoadedNodeForService(String service) {
        Map<String, Integer> m =this.getAllVMs(service);
        
        if(m==null)
            return null;
        //System.out.println("WITHIN GLOBAL STORAGE\n"+m);
//        ArrayList<String> str = new ArrayList(m.keySet());
//        int len = Integer.MAX_VALUE;
//        String vm=null;
//        for(String s: str)
//        {
//            int temp=0;
//            boolean flag=false;
//            //System.out.println("WITHIN GLOBAL STORAGE Server\n"+s);
//            
//            for(Pair<String,String> sp : service_load.keySet())
//            {
//                System.out.println(sp.getKey()+" "+sp.getValue()+" "+service+" "+s);
//                if(s.equals(sp.getKey()) && service.equals(sp.getValue()))
//                    flag=true;
//            }
//            
//            if(flag)
//            {
//                //System.out.println("ENTERED"+s);
//                for(Pair<String,String> sp : service_load.keySet())
//                {
//                    if(s.equals(sp.getKey()) && service.equals(sp.getValue()))
//                        temp=service_load.get(sp);
//                }
//                if(len>temp)
//                {
//                    len=temp;
//                    vm=s;
//                }
//            }
//        }
        String vm=null;
        int low = Integer.MAX_VALUE;
        int t;
        Map<String,Integer> temp = service_load_new.get(service);
        //System.out.println(temp);
        for(String v: temp.keySet())
        {
            t=temp.get(v);
            if(t<low)
            {
                low=t;
                vm=v;
            }
        }
        return vm;
    }
    
    @Override
    public synchronized Map<String,Map<String,Integer> >  getServiceOnVMLoad() {
		// TODO Auto-generated method stub
        return new HashMap(service_load_new);

	}

    @Override
    public synchronized void increaseLoad(String vm) {
        cpuload.put(vm,cpuload.get(vm)+1);
    }

    @Override
    public synchronized void updateCPULoad(Map<String, Integer> mp) {
        for(String vm: mp.keySet())
        {
            cpuload.put(vm,mp.get(vm));
        }
    }

    @Override
    public synchronized void addCpu(String vm) {
        cpuload.put(vm,1);
    }

    @Override
    public synchronized void addCpuLoad(String vm, Integer load) {
        cpuload.put(vm, load);
    }
    
    
    
    
}