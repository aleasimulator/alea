/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.plugins;

import xklusac.environment.ComplexGridlet;
import xklusac.environment.ResultCollector;
import xklusac.environment.SchedulerData;

/**
 *
 * @author Gabriela Podolnikova
 */
public class MyPlugin extends AbstractPlugin {

    @Override
    public void cumulate(ComplexGridlet gridletReceived) {
    }

    @Override
    public Double calculate(ResultCollector rc, SchedulerData sd) {
        return 0d;
    }
    
}
