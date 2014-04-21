package xklusac.environment;

/**
 * This class stores the simulation data for the scheduler entity.
 * This data can be used for the calculation of simulation metrics.
 * 
 * @author Gabriela Podolnikova
 */
public class SchedulerData {
    private double av_PEs;
    private double wav_PEs;
    private double failureTime;
    private double wfailureTime;
    private double clock;
    private double runtime;
    private double classicLoad;
    private double maxLoad;
    private int submitted;
    
    /**
     * Creates a new instance of SchedulerData.
     * 
     * @param av_PEs
     * @param wav_PEs
     * @param failureTime
     * @param wfailureTime
     * @param clock
     * @param runtime
     * @param classicLoad
     * @param maxLoad
     * @param submitted 
     */
    public SchedulerData (double av_PEs, double wav_PEs, double failureTime, double wfailureTime,
            double clock, double runtime, double classicLoad, double maxLoad, int submitted) {
        this.av_PEs = av_PEs;
        this.wav_PEs = wav_PEs;
        this.failureTime = failureTime;
        this.wfailureTime = wfailureTime;
        this.clock = clock;
        this.runtime = runtime;
        this.classicLoad = classicLoad;
        this.maxLoad = maxLoad;
        this.submitted = submitted;
    }

    /**
     * @return the av_PEs
     */
    public double getAv_PEs() {
        return av_PEs;
    }

    /**
     * @return the wav_PEs
     */
    public double getWav_PEs() {
        return wav_PEs;
    }

    /**
     * @return the failureTime
     */
    public double getFailureTime() {
        return failureTime;
    }

    /**
     * @return the wfailureTime
     */
    public double getWfailureTime() {
        return wfailureTime;
    }

    /**
     * @return the clock
     */
    public double getClock() {
        return clock;
    }

    /**
     * @return the runtime
     */
    public double getRuntime() {
        return runtime;
    }

    /**
     * @return the classicLoad
     */
    public double getClassicLoad() {
        return classicLoad;
    }

    /**
     * @return the maxLoad
     */
    public double getMaxLoad() {
        return maxLoad;
    }

    /**
     * @return the submitted
     */
    public int getSubmitted() {
        return submitted;
    }
    
}
