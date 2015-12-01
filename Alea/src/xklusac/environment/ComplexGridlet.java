package xklusac.environment;

import gridsim.*;
import java.util.LinkedList;

/**
 * Class ComplexGridlet<p>
 * This class represents one gridlet i.e. one job and its parameters. Job may
 * require 1 or more CPUs for its run.
 *
 * @author Dalibor Klusacek
 */
public class ComplexGridlet extends Gridlet {

    /**
     * required architecture
     */
    private String archRequired;
    /**
     * required OS
     */
    private String osRequired;
    /**
     * arrival time i.e. time of gridlet arrival in the system
     */
    private double arrival_time;
    /**
     * release date i.e. how long from start time the gridlet can be started
     */
    private double release_date;
    /**
     * due date (deadline)
     */
    private double due_date;
    /**
     * gridlet priority
     */
    private int priority;
    /**
     * required number of CPU
     */
    private int numPE;
    /**
     * expected computational length
     */
    private double estimatedLength;
    /**
     * unused
     */
    private double estimatedMachine;
    /**
     * queue name where the job was submitted in
     */
    private String queue;
    private String properties;
    private boolean repeated;
    private String user = "";
    private LinkedList<Integer> PEs = new LinkedList();
    private long job_limit;
    private double expectedFinishtime;
    private double percentage;
    private long ram;
    private int numNodes;
    private int ppn;
    private boolean backfilled;

    private String onJobStart = null;
    private String onJobCompl = null;
    private String onJobFail = null;

    public String getOnJobStart() {
        return onJobStart;
    }

    public String getOnJobCompl() {
        return onJobCompl;
    }
    
    public String getOnJobFail() {
        return onJobFail;
    }

    public void setOnJobStart(String agent) {
        onJobStart = agent;
    }

    public void setOnJobCompl(String agent) {
        onJobCompl = agent;
    }
    
    public void setOnJobFail(String agent) {
        onJobFail = agent;
    }

    /**
     * Creates a new instance of ComplexGridlet representing one Job
     *
     * @param gridletID id of this gridlet
     * @param gridletLength computational length in MI
     * @param gridletFileSize size in Bytes
     * @param gridletOutputSize output size in Bytes
     * @param oSrequired Operating System required to run this gridlet
     * @param archRequired required architecture
     * @param release_date release date of this gridlet
     * @param due_date due date of this gridlet
     * @param priority priority of this job
     * @param numPE number of requested PEs
     * @param queue queue name where the job was submitted in
     * @param properties list of comma separated properties required by this job
     * @param ram RAM in KB required by this job
     */
    public ComplexGridlet(int gridletID, String user, long job_limit, double gridletLength, double estimatedLength, long gridletFileSize,
            long gridletOutputSize, String oSrequired, String archRequired,
            double arrival_time, double due_date, int priority, int numPE, double estMach, String queue, String properties, double percentage, long ram, int numNodes, int ppn) {
        // call Gridlet constructor
        super(gridletID, gridletLength, gridletFileSize, gridletOutputSize);
        this.setOpSystemRequired(oSrequired);
        this.setArchRequired(archRequired);
        this.setArrival_time(arrival_time);
        this.setRelease_date(arrival_time);
        this.setDue_date(due_date);
        this.setPriority(priority);
        this.setNumPE(numPE);
        this.setEstimatedLength(estimatedLength);
        this.setEstimatedMachine(estMach);
        this.setQueue(queue);
        this.setRepeated(false);
        this.setProperties(properties);
        this.setUser(user);
        this.setJobLimit(job_limit);
        this.setExpectedFinishTime(0.0);
        this.setPercentage(percentage);
        this.setRam(ram);
        this.setPpn(ppn);
        this.setNumNodes(numNodes);
    }

    /**
     * Getter method
     */
    public String getOpSystemRequired() {
        return osRequired;
    }

    /**
     * Setter method
     */
    public void setOpSystemRequired(String osRequired) {
        this.osRequired = osRequired;
    }

    /**
     * Getter method
     */
    public double getArrival_time() {
        return arrival_time;
    }

    /**
     * Setter method
     */
    public void setArrival_time(double start_time) {
        this.arrival_time = start_time;
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
    public void setArchRequired(String archRequired) {
        this.archRequired = archRequired;
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
    public int getPriority() {
        return priority;
    }

    /**
     * Setter method
     */
    public void setPriority(int priority) {
        this.priority = priority;
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
     * Getter method
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

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
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

    public LinkedList<Integer> getPEs() {
        return PEs;
    }

    public void setPEs(LinkedList<Integer> PEs) {
        this.PEs = PEs;
    }

    public long getJobLimit() {
        return job_limit;
    }

    public void setJobLimit(long job_limit) {
        this.job_limit = job_limit;
    }

    public double getExpectedFinishTime() {
        return this.expectedFinishtime;
    }

    public void setExpectedFinishTime(double expectedFinishtime) {
        this.expectedFinishtime = expectedFinishtime;
        //System.out.println(this.getGridletID()+" gr "+expectedFinishtime);
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
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
     * @return the backfilled
     */
    public boolean isBackfilled() {
        return backfilled;
    }

    /**
     * @param backfilled the backfilled to set
     */
    public void setBackfilled(boolean backfilled) {
        this.backfilled = backfilled;
    }

}
