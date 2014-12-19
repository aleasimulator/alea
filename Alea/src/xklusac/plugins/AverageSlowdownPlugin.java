package xklusac.plugins;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;

/**
 * Class AverageSlowdownPlugin represents the plugin for average slowdown.
 * 
 * @author Gabriela Podolnikova
 */
public class AverageSlowdownPlugin extends AbstractPlugin implements Plugin {

    private double slowdown;
    
    /**
     * @see Plugin#cumulate(xklusac.environment.ComplexGridlet) 
     */
    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
        double finish_time = gridletReceived.getFinishTime();
        double cpu_time = gridletReceived.getActualCPUTime();
        double arrival = gridletReceived.getArrival_time();
        double response = Math.max(0.0, (finish_time - arrival));
        slowdown += Math.max(1.0, (response / Math.max(1.0, cpu_time)));
    }

    /**
     * @see Plugin#calculate(xklusac.environment.ResultCollector, xklusac.environment.SchedulerData) 
     */
    @Override
    public Double calculate(ResultCollector rc, SchedulerData sd) {
        double avgSlowdown = slowdown / rc.getReceived();
        return avgSlowdown;
    }
    
}
