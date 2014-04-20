package xklusac.plugins;

import java.util.Map;
import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;

/**
 * Interface Plugin defines the methodes that all plugins should override.
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
     * Getter for plugin configuration.
     * 
     * @return map with plugin configuration
     */
    Map<String, String> getPluginConfiguration();

    /**
     * Cumulates data for the metric calculation.
     * 
     * @param gridletReceived
     *            gridlet representing one job.
     */
    void cumulate(ComplexGridlet gridletReceived);
    
    /**
     * Compute metric with given intitialization and cumulation.
     * 
     * @param rc
     *        an instance of the ResultCollector class
     * @param sd 
     *        an instance of the SchedulerData class
     * @return String with calculated result.
     */
    Double calculate (ResultCollector rc, SchedulerData sd);
}
