/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

import xklusac.plugins.PluginFactory;
import xklusac.plugins.Plugin;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Gabi
 */
public class FactoryPluginTest {
    static Map<String, String> customProperties;
    
    @BeforeClass
    public static void setUp() throws Exception {
        customProperties = new HashMap<String, String>();
    }
    
    @Test
    public void createPlugin() {
        Plugin pl = PluginFactory.createPlugin("xklusac.environment.SamplePlugin");
        assertNotNull(pl);
        System.out.println("pl: " + pl.getClass().getName());
    }
    
    @Test
    public void init() {
        Plugin pl = PluginFactory.createPlugin("xklusac.environment.SamplePlugin");
        pl.init(customProperties);
    }
    
    /*@Test
    public void processData() {
        Plugin pl = PluginFactory.createPlugin("xklusac.environment.SamplePlugin");
        Object in = new Object();
        Object out = pl.processData(in);
    }*/
}
