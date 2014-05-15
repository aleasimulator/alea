package xklusac.plugins;

import java.util.Map;
import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;

/**
 * Alea supports custom simulation metrics. These metrics can be implemented
 * as plugins. This interface defines the methods that all plugins should override.
 * 
 * @author Gabriela Podolnikova
 */
public interface Plugin {
    /**
     * Initially called method to serve plugin initialization.
     * 
     * @param pluginConfiguration
     *            the custom properties Map instance. This map provides custom
     *            (keyword, value) tuples.
     */
    void init(Map<String, String> pluginConfiguration);

    /**
     * Cumulates data for the metric calculation.
     * 
     * @param gridletReceived
     *            gridlet representing one job.
     */
    void cumulate(ComplexGridlet gridletReceived);
    
    /**
     * Compute the final result of the metric.
     * 
     * @param rc
     *        an instance of the ResultCollector class
     * @param sd 
     *        an instance of the SchedulerData class
     * @return String with the calculated result.
     */
    Double calculate (ResultCollector rc, SchedulerData sd);
}
