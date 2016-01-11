/*
 * JobLoader.java
 *
 * Created on 4. listopad 2009, 14:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xklusac.environment;

/**
 * Class JobLoader<p>
 * generates the workload for the simulation. PWA, GWA, MetaCentrum and Pisa workloads are understood. Selction is done via file extension (swf,gwf,mwf,pwf).
 * Alea 4.0 supports <b>GRID WORKLOADS FORMAT (GWF)</b> which is described at the <b>GRID WORKLOAD ARCHIVE (GWA)</b>: <a href="http://gwa.ewi.tudelft.nl">http://gwa.ewi.tudelft.nl</a><p>
 * Alea 4.0 supports <b>STANDARD WORKLOADS FORMAT (SWF)</b> which is described at the <b>PARALLEL WORKLOADS ARCHIVE (GWA)</b>: <a href="http://www.cs.huji.ac.il/labs/parallel/workload/">http://www.cs.huji.ac.il/labs/parallel/workload/</a><p>
 * Alea 4.0 supports <b>MetaCentrum WORKLOAD FORMAT (MWF)</b> which is described at: <a href="http://www.fi.muni.cz/~xklusac/workload/">http://www.fi.muni.cz/~xklusac/workload/</a><p>
 * Alea 4.0 supports <b>PISA WORKLOAD FORMAT (PWF)</b> which is described at: <a href="http://www.fi.muni.cz/~xklusac/alea/">http://www.fi.muni.cz/~xklusac/alea/</a><p>
 * @author Dalibor Klusacek
 */
public class JobLoader {
    
    String name;
    double baudRate;
    int total_jobs;
    String data_set;
    int maxPE;
    int minPErating;
    int maxPErating;
    double multiplier;
    int exp;
    int totPEs;
    boolean estimates;
    
    /** Creates a new instance of JobLoader */
    public JobLoader(String name, double baudRate, int total_jobs, String data_set, int maxPE, int minPErating, int maxPErating, double multiplier, 
            int exp, int totPEs, boolean estimates) {
        this.name = name;
        this.baudRate = baudRate;
        this.total_jobs = total_jobs;
        this.data_set = data_set;
        this.maxPE = maxPE;
        this.minPErating = minPErating;
        this.maxPErating = maxPErating;
        this.multiplier = multiplier;
        this.exp = exp;
        this.totPEs = totPEs;
        this.estimates = estimates;
        init(data_set);
    }
    /** Creates the Loader entity that will send jobs to the Scheduler */
    private void init(String set){
        try {         
            if(set.contains("gwf")){
                GWFLoader gwa_loader = new GWFLoader(name, baudRate, total_jobs, data_set, maxPE, minPErating, maxPErating);                
            }else if(set.contains("swf")){
                SWFLoader pwa_loader = new SWFLoader(name, baudRate, total_jobs, data_set, maxPE, minPErating, maxPErating);
            }else if(set.contains("mwf")){
                MWFLoader meta_loader = new MWFLoader(name, baudRate, total_jobs, data_set, maxPE, minPErating, maxPErating, multiplier,totPEs, estimates);
            }else if(set.contains("pwf")){
                PWFLoader pisa_loader = new PWFLoader(name, baudRate, total_jobs, data_set, maxPE, minPErating, maxPErating, exp);
            }else if (set.contains("dyn")){
                DynamicLoader dyn_loader = new DynamicLoader(name, baudRate, total_jobs, data_set, maxPE, minPErating, maxPErating);
            }else{
                System.out.println("Wrong workload format or file extension (gwf,swf,mwf,pwf,ai)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
}
