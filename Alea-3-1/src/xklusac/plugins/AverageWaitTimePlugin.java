package xklusac.plugins;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;

/**
 * Class AverageWaitTimePlugin represents the plugin for average wait time.
 * @author Gabriela Podolnikova
 */
public class AverageWaitTimePlugin extends AbstractPlugin implements Plugin {
    
    private double waitTime;

    /**
     * @see Plugin
     */
    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
        double finish_time = gridletReceived.getFinishTime();
        double cpu_time = gridletReceived.getActualCPUTime();
        double arrival = gridletReceived.getArrival_time();
        double response = Math.max(0.0, (finish_time - arrival));
        waitTime += Math.max(0.0, (response - cpu_time));
    }

    /**
     * @see Plugin
     */    
    @Override
    public Double calculate(ResultCollector rc, SchedulerData sd) {
        double avgWaitTime = Math.round((waitTime / rc.getReceived()) * 100) / 100.0;
        return avgWaitTime;
    }
    
    
}
