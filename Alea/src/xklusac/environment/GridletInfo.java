package xklusac.environment;

import gridsim.GridSim;
import java.util.HashMap;
import java.util.LinkedList;
//import gridsim.*;

/**
 * Class GridletInfo<p>
 * This class creates an object handling information about gridlets. It use set
 * / get methods to set / get information about gridlet. It stores various
 * information of real gridlet. If more information required, this is the right
 * place to store them (see set/getTardiness(gridletInfo) method) rather then
 * changing ComplexGridlet class (if possible).
 *
 * @author Dalibor Klusacek
 */
public class GridletInfo {

    /**
     * owner id
     */
    private int ownerID;
    /**
     * gridlet id
     */
    private int ID;
    /**
     * selected resource id
     */
    private int resourceID;
    /**
     * gridlet status
     */
    private int status;
    /**
     * computational length
     */
    private double length;
    /**
     * not used
     */
    private double finishedSoFar;
    /**
     * not used
     */
    private double cost;
    /**
     * not used
     */
    private double completitionFactor;
    /**
     * link to original gridlet
     */
    private ComplexGridlet gl;
    /**
     * architecture required by the gridlet
     */
    private String archRequired;
    /**
     * OS required by the gridlet
     */
    private String osRequired;
    /**
     * release date (start time)
     */
    private double release_date;
    /**
     * due date (deadline)
     */
    private double due_date;
    /**
     * Expected tardiness calculated by the scheduler
     */
    private double tardiness;
    /**
     * It denotes this dynamicaly changing information: time_to_release =
     * max(0.0, (arrival_time + release_date) - current_time)
     */
    private double time_to_release;
    /**
     * gridlet priority
     */
    private double priority;
    /**
     * number of PEs to run this gridlet
     */
    private int numPE;
    /**
     * expected finish time - for schedule only
     */
    private double expectedFinishTime;
    /**
     * expected start time - for schedule only
     */
    private double expectedStartTime;
    /**
     * estimatedLength length - not used
     */
    private double estimatedLength;
    /**
     * machine's PE rating used to estimate the gridlet lenght -not used
     */
    private double estimatedMachine;
    /**
     * queue where this gridlet was submitted
     */
    private String queue;
    private String properties;
    private LinkedList<Integer> PEs = new LinkedList();
    private String user = "";
    private double avg_length = 0.0;
    private double last_length = 0.0;
    private long jobLimit = 0;
    private double percentage;
    private boolean init;
    private long ram;
    private int numNodes;
    private int ppn;

    private HashMap<Integer, Boolean> resourceSuitable;

    /**
     * Creates a new instance of GridletInfo object based on the "real" gridlet
     *
     * @param gl - Gridlet - the constructor gets the important informations
     * about gridlet and sets them to inner variables
     */
    public GridletInfo(ComplexGridlet gl) {

        this.setOwnerID(gl.getUserID());
        this.setID(gl.getGridletID());
        this.setResourceID(gl.getResourceID());
        this.setStatus(gl.getGridletStatus());
        this.setLength(gl.getGridletLength());
        this.setFinishedSoFar(gl.getGridletFinishedSoFar());
        this.setCompletitionFactor(gl.getGridletFinishedSoFar() / gl.getGridletLength());
        this.setGridlet(gl);
        this.setOsRequired(gl.getOpSystemRequired());
        this.setArchRequired(gl.getArchRequired());
        this.setRelease_date(gl.getRelease_date());
        this.setDue_date(gl.getDue_date());
        this.setTardiness(0.0);
        this.setTime_to_release(0.0);
        this.setPriority(gl.getPriority());
        this.setNumPE(gl.getNumPE());
        this.setExpectedFinishTime(GridSim.clock() + gl.getJobLimit());
        this.setEstimatedLength(gl.getEstimatedLength());
        this.setEstimatedMachine(gl.getEstimatedMachine());
        this.setQueue(gl.getQueue());
        this.setExpectedStartTime(GridSim.clock() + 0.0);
        this.setProperties(gl.getProperties());
        this.setUser(gl.getUser());
        this.setAvg_length(0.0);
        this.setJobLimit(gl.getJobLimit());
        this.setPercentage(gl.getPercentage());
        this.setInit(true);
        this.setRam(gl.getRam());
        this.setNumNodes(gl.getNumNodes());
        this.setPpn(gl.getPpn());
        this.setResourceSuitable(new HashMap());
    }

    /**
     * Getter method
     */
    public int getOwnerID() {
        return ownerID;
    }

    /**
     * Setter method
     */
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    /**
     * Getter method
     */
    public int getID() {
        return ID;
    }

    /**
     * Setter method
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Getter method
     */
    public int getResourceID() {
        return resourceID;
    }

    /**
     * Setter method
     */
    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    /**
     * Getter method
     */
    public int getStatus() {
        this.status = getGridlet().getGridletStatus(); // essential for fresh information
        return status;
    }

    /**
     * Setter method
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Getter method
     */
    public double getLength() {
        return length;
    }

    /**
     * Setter method
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * Getter method
     */
    public double getFinishedSoFar() {
        this.finishedSoFar = getGridlet().getGridletFinishedSoFar(); // essential for fresh information
        return finishedSoFar;
    }

    /**
     * Setter method
     */
    public void setFinishedSoFar(double finishedSoFar) {
        this.finishedSoFar = finishedSoFar;
    }

    /**
     * Getter method
     */
    public double getCompletitionFactor() {
        return completitionFactor;
    }

    /**
     * Setter method
     */
    public void setCompletitionFactor(double completitionFactor) {
        this.completitionFactor = completitionFactor;
    }

    /**
     * Getter method
     */
    public String getArchRequired() {
        return archRequired;
    }

    /**
     * Setter method
     */
    public void setArchRequired(String osRequired) {
        this.archRequired = osRequired;
    }

    /**
     * Getter method
     */
    public String getOsRequired() {
        return osRequired;
    }

    /**
     * Setter method
     */
    public void setOsRequired(String osRequired) {
        this.osRequired = osRequired;
    }

    /**
     * Getter method
     */
    public ComplexGridlet getGridlet() {
        return gl;
    }

    /**
     * Setter method
     */
    public void setGridlet(ComplexGridlet gl) {
        this.gl = gl;
    }

    /**
     * Getter method
     */
    public double getRelease_date() {
        return release_date;
    }

    /**
     * Setter method
     */
    public void setRelease_date(double release_date) {
        this.release_date = release_date;
    }

    /**
     * Getter method
     */
    public double getDue_date() {
        return due_date;
    }

    /**
     * Setter method
     */
    public void setDue_date(double due_date) {
        this.due_date = due_date;
    }

    /**
     * Getter method
     */
    public double getTardiness() {
        return tardiness;
    }

    /**
     * Setter method
     */
    public void setTardiness(double tardiness) {
        this.tardiness = tardiness;
    }

    /**
     * Getter method
     */
    public double getTime_to_release() {
        return time_to_release;
    }

    /**
     * Setter method
     */
    public void setTime_to_release(double time_to_release) {
        this.time_to_release = time_to_release;
    }

    /**
     * Getter method
     */
    public double getPriority() {
        return priority;
    }

    /**
     * Setter method
     */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * Getter method
     */
    public int getNumPE() {
        return numPE;
    }

    /**
     * Setter method
     */
    public void setNumPE(int numPE) {
        this.numPE = numPE;
    }

    /**
     * Getter method
     */
    public double getExpectedFinishTime() {
        return expectedFinishTime;
    }

    /**
     * Setter method
     */
    public void setExpectedFinishTime(double expectedFinishTime) {
        this.expectedFinishTime = expectedFinishTime;
        this.getGridlet().setExpectedFinishTime(expectedFinishTime);
    }

    /**
     * Getter method
     */
    public double getEstimatedLength() {
        return estimatedLength;
    }

    /**
     * Setter method
     */
    public void setEstimatedLength(double estimated) {
        this.estimatedLength = estimated;
    }

    /**
     * Setter method
     */
    public double getEstimatedMachine() {
        return estimatedMachine;
    }

    /**
     * Setter method
     */
    public void setEstimatedMachine(double estimatedMachine) {
        this.estimatedMachine = estimatedMachine;
    }

    /**
     * Getter method
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Setter method
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * Getter method
     */
    public double getExpectedStartTime() {
        return expectedStartTime;
    }

    /**
     * Setter method
     */
    public void setExpectedStartTime(double expectedStartTime) {
        this.expectedStartTime = expectedStartTime;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getAvg_length() {
        return avg_length;
    }

    public void setAvg_length(double avg_length) {
        this.avg_length = avg_length;
    }

    public LinkedList<Integer> getPEs() {
        return this.getGridlet().getPEs();
    }

    public void setPEs(LinkedList<Integer> PEs) {
        this.getGridlet().setPEs(PEs);
    }

    public double getJobRuntime(int peRating) {
        if (ExperimentSetup.estimates) {
            if (ExperimentSetup.use_AvgLength) {
                //System.out.println("avg length ===== "+Math.round(this.getAvg_length() / peRating)+" ? "+Math.round(this.getLast_length() / peRating));
                return Math.min(jobLimit, Math.max(0.0, (this.getAvg_length() / peRating)));
            } else if (ExperimentSetup.use_LastLength) {
                //System.out.println(this.getID()+" last length = "+Math.min(jobLimit, Math.max(0.0, (this.getLast_length() / peRating)))+" / job limit = "+jobLimit+" user = "+this.getUser());
                return Math.min(jobLimit, Math.max(0.0, (ExperimentSetup.runtime_multiplicator * (this.getLast_length() / peRating))));
            }/* else if (ExperimentSetup.useUserPrecision) {
             //System.out.println("last length ===== "+jobLimit);
             double real_runtime = Math.max(0.0, (this.getLength() / peRating));
             double diff = (jobLimit - real_runtime) / 100.0;
             diff = diff * this.getPercentage();
             //System.out.println(this.getID()+": limit = "+jobLimit+", estimate increased by "+this.getPercentage()+"% from "+Math.round(real_runtime)+" to "+Math.round(real_runtime+diff)+" which is "+Math.round(((Math.round(real_runtime+diff)*100)/real_runtime))+"% of real runtime");
             // return real runtime + additional time as "added" bu user estimate
             return Math.min(jobLimit, Math.max(0.0, (real_runtime + diff)));
             } else if (ExperimentSetup.useDurationPrecision) {
             //System.out.println("last length ===== "+jobLimit);
             double real_runtime = Math.max(0.0, (this.getLength() / peRating));
             double diff = (real_runtime) / 100.0;
             diff = diff * this.getPercentage();
             //System.out.println(this.getID()+": D_limit = "+jobLimit+", estimate (in/de)creased by "+this.getPercentage()+"% from "+Math.round(real_runtime)+" to "+Math.round(real_runtime+diff)+" which is "+Math.round(((Math.round(real_runtime+diff)*100)/real_runtime))+"% of real runtime");
             //System.out.println(this.getID()+": D_limit = "+jobLimit+", real runtime in/de creased by "+this.getPercentage()+" % from "+Math.round(real_runtime)+" to "+Math.round(real_runtime+diff)+" which is "+((Math.round(real_runtime+diff))/real_runtime));
             // return real runtime + additional time as "added" bu user estimate


             return Math.min(jobLimit, Math.max(0.0, (real_runtime + diff)));
             }*/ else {

                double REAL_RUNTIME = Math.max(0.0, (this.getLength() / peRating));
                double RUNTIME = jobLimit;
                /*if (RUNTIME == 86400) {
                 //6 8 12 16 20
                 if (REAL_RUNTIME < (5 * 3600)) {
                 RUNTIME = 5 * 3600;
                 } else if (REAL_RUNTIME < (6 * 3600)) {
                 RUNTIME = 6 * 3600;
                 } else if (REAL_RUNTIME < (8 * 3600)) {
                 RUNTIME = 8 * 3600;
                 } else if (REAL_RUNTIME < (12 * 3600)) {
                 RUNTIME = 12 * 3600;
                 } else if (REAL_RUNTIME < (16 * 3600)) {
                 RUNTIME = 16 * 3600;
                 } else if (REAL_RUNTIME < (20 * 3600)) {
                 RUNTIME = 20 * 3600;
                 }
                 }
                 if (RUNTIME == 14400) {
                 //6 8 12 16 20
                 if (REAL_RUNTIME < (3 * 3600)) {
                 RUNTIME = 3 * 3600;
                 }
                 }*/

                return RUNTIME;
                //System.out.println("job limit length ===== "+jobLimit);
                //return jobLimit;
            }
        } else {
            /*
             * if(jobLimit < Math.max(0.0, (this.getLength() / peRating)) &&
             * ExperimentSetup.use_tsafrir){ System.out.println(this.getID()+"
             * limit < runtime by "+Math.round(Math.max(0.0, (this.getLength() /
             * peRating))-jobLimit)+" seconds."); }
             */
            return Math.min(jobLimit, Math.max(0.0, (this.getLength() / peRating)));
        }

    }

    public double getJobLimit() {
        return this.jobLimit;
    }

    public void setJobLimit(long jobLimit) {
        this.jobLimit = jobLimit;
    }

    public double getLast_length() {
        return last_length;
    }

    public void setLast_length(double last_length) {
        this.last_length = last_length;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    /**
     * @return the ram
     */
    public long getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(long ram) {
        this.ram = ram;
    }

    /**
     * @return the numNodes
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * @param numNodes the numNodes to set
     */
    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    /**
     * @return the ppn
     */
    public int getPpn() {
        return ppn;
    }

    /**
     * @param ppn the ppn to set
     */
    public void setPpn(int ppn) {
        this.ppn = ppn;
    }

    /**
     * @return the resourceSuitable
     */
    public HashMap getResourceSuitable() {
        return resourceSuitable;
    }

    /**
     * @param resourceSuitable the resourceSuitable to set
     */
    public void setResourceSuitable(HashMap resourceSuitable) {
        this.resourceSuitable = resourceSuitable;
    }
}
