package xklusac.plugins;

import xklusac.plugins.Plugin;

/**
 * Class PluginFactory is used for creating Plugin instances.
 * 
 * @author Gabriela Podolnikova
 */
public class PluginFactory {
    /**
     * Create a Plugin instance. The creation is based on using the reflection,
     * hence the caller must have sufficient security privilege etc.
     *
     * @param className
     *            the class name; it must be a valid class name on the classpath;
     *            the class must implement the Plugin interface
     *
     * @return the instance of the plugin, but not initialized yet
     *
     * @throws RuntimeException
     *             if the plugin could not be properly created
     */
    public static Plugin createPlugin(String className) {
        try {
            // Make the instance of the plugin
            final Class<?> pluginClass = Class.forName(className);
            final Object result = pluginClass.newInstance();

            return (Plugin)result;
        } catch (SecurityException e) {
            throw new RuntimeException("Could not create plugin: " + className, e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not create plugin: " + className, e);
        } catch (ExceptionInInitializerError e) {
            throw new RuntimeException("Could not create plugin: " + className, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not create plugin: " + className, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not create plugin: " + className, e);
        }
    }
}
