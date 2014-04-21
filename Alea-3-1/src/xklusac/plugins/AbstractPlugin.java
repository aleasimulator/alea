package xklusac.plugins;

import java.util.Collections;
import java.util.Map;

/**
 * Class AbstractPlugin provides a common basis for custom plugins.
 * 
 * @author Gabriela Podolnikova
 */
abstract class AbstractPlugin implements Plugin {
    
    
    private Map<String, String> pluginConfiguration;
    
    /**
     * @see Plugin#init(java.util.Map) 
     */
    @Override
    public void init(Map<String, String> pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }
    
    /**
     * @see Plugin#getPluginConfiguration() 
     */
    @Override
    public Map<String, String> getPluginConfiguration() {
        return Collections.unmodifiableMap(pluginConfiguration);
    }
    
}
