package xklusac.plugins;

import java.util.Collections;
import java.util.Map;

/**
 * Class AbstractPlugin implements the functonality of plugins that is the same for all plugins.
 * @author Gabriela Podolnikova
 */
abstract class AbstractPlugin implements Plugin {
    
    
    private Map<String, String> pluginConfiguration;
    
    /**
     * @see Plugin
     */
    @Override
    public void init(Map<String, String> pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }
    
    /**
     * @see Plugin
     */
    @Override
    public Map<String, String> getPluginConfiguration() {
        return Collections.unmodifiableMap(pluginConfiguration);
    }
    
}
