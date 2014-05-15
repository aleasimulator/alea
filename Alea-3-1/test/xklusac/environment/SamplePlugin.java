package xklusac.environment;

import xklusac.plugins.Plugin;
import java.util.Map;

/**
 *
 * @author Gabi
 */
public class SamplePlugin implements Plugin {

    @Override
    public void init(Map<String, String> customProperties) {
       System.out.println("Toto je metoda init");
    }

    /*@Override
    public Object processData(Object completeTaskLog) {
        System.out.println("Toto je metoda processData");
        return new Object();
    }*/
    
    @Override
    public void cumulate(ComplexGridlet gridletReceived){
        System.out.println("Toto je metoda cumulate");
    }
    
    @Override
    public Double calculate (ResultCollector rc, SchedulerData sd){
        return 0.2;
    }
    
}
