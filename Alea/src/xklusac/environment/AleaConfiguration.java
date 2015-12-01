package xklusac.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import xklusac.plugins.PluginConfiguration;

/**
 * This class provides access to property files. These files hold the
 * configuration for the simulation or the descriptions of the configurable
 * items.
 *
 * @author Gabriela Podolnikova
 */
public class AleaConfiguration {

    public static final String DEFAULT_FILE_NAME = "configuration.properties";
    public static final String HEADER = "HEADER";
    public static final String PLUGINS = "plugins";
    public static final String PLUGIN = "plugin.";
    private /*static*/ final Properties props = new Properties();
    private final String fileName;
    private final InputStreamProvider inputStreamProvider;

    /**
     * Creates a new instance of AleaConfiguration. Loads the configuration
     * file.
     *
     * @throws IOException if the configuration file cannot be loaded.
     */
    public AleaConfiguration() throws IOException {
        this(DEFAULT_FILE_NAME);
    }

    /**
     * Creates a new instance of AleaConfiguration. Loads the file with the
     * given path.
     *
     * @param path the path to the file that should be loaded
     *
     * @throws IOException if the file cannot be loaded.
     */
    public AleaConfiguration(String path) throws IOException {
        fileName = path;
        inputStreamProvider = null;
        InputStream is = new FileInputStream(path);
        props.load(is);
        is.close();
    }

    /**
     * Creates a new instances of AleaConfiguration. Loads the given stream.
     *
     * @param inputStreamProvider stream to be loaded
     *
     * @throws IOException if the file cannot be loaded.
     */
    public AleaConfiguration(InputStreamProvider inputStreamProvider) throws IOException {
        fileName = null;
        this.inputStreamProvider = inputStreamProvider;
        InputStream is = inputStreamProvider.getInputStream();
        props.load(is);
        is.close();
    }

    /**
     * Gets the configuration for one plugin at the given index.
     *
     * @param pluginIndex index of the plugin
     *
     * @return map with the plugin configuration
     */
    public Map<String, String> getPluginConfiguration(int pluginIndex) {
        Map<String, String> plugincfg = new HashMap<String, String>();
        String key;
        String startOfKey = PLUGIN + pluginIndex + ".";
        int index = startOfKey.length();
        List<String> keyList = new ArrayList<String>();
        try {
            keyList = this.getKeyList();
        } catch (IOException ex) {
            Logger.getLogger(AleaConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String s : keyList) {
            if (s.startsWith(startOfKey)) {
                key = s.substring(index);
                plugincfg.put(key, this.getString(s));
            }
        }
        return plugincfg;
    }

    /**
     * Returns the key for a plugin in the configuration file.
     *
     * @param index the index of the plugin in the plugin array
     * @param pluginKey the key of one plugin
     * @param common if false then the index is added into the returned string
     *
     * @return plugin key
     */
    public static String getPluginConfigurationKey(int index, String pluginKey, boolean common) {
        if (common) {
            return PLUGIN + pluginKey;
        } else {
            return PLUGIN + index + "." + pluginKey;
        }
    }

    public File getFile() {
        File f = new File(getFileName());
        return f;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Gets String value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as String
     */
    public String getString(String key) {
        String str;
        str = props.getProperty(key);
        return str;
    }

    /**
     * Gets int value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as int
     */
    public int getInt(String key) {
        String value = props.getProperty(key);
        return extractInt(value);
    }

    private static int extractInt(String value) {
        int i;
        i = Integer.parseInt(value);
        return i;
    }

    /**
     * Gets double value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as double
     */
    public double getDouble(String key) {
        String value = props.getProperty(key);
        return extractDouble(value);
    }

    private static double extractDouble(String value) {
        double d;
        d = Double.parseDouble(value);
        return d;
    }

    /**
     * Gets int array value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as array of ints
     */
    public int[] getIntArray(String key) {
        String value = props.getProperty(key);
        return extractIntArray(value);
    }

    private static int[] extractIntArray(String value) {
        String[] s = value.split(",");
        final int[] ints = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            ints[i] = Integer.parseInt(s[i].trim());
        }
        return ints;
    }

    /**
     * Gets boolean value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as boolean
     */
    public boolean getBoolean(String key) {
        String value = props.getProperty(key);
        return extractBoolean(value);
    }

    private static boolean extractBoolean(String value) {
        boolean bool;
        if ((!value.equalsIgnoreCase("true")) && (!value.equalsIgnoreCase("false"))) {
            throw new IllegalArgumentException("Not a boolean: " + value);
        }
        bool = Boolean.parseBoolean(value);
        return bool;
    }

    /**
     * Gets String array value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as String array
     */
    public String[] getStringArray(String key) {
        String value = props.getProperty(key);
        return extractStringArray(value);
    }

    private static String[] extractStringArray(String value) {
        String[] s = value.split(",");
        return s;
    }

    /**
     * Gets boolean array value from properties.
     *
     * @param key the configuration item key
     *
     * @return the value as boolean array
     */
    public boolean[] getBooleanArray(String key) {
        String value = props.getProperty(key);
        return extractBooleanArray(value);
    }

    private static boolean[] extractBooleanArray(String value) {
        String[] s = value.split(",");
        final boolean[] bools = new boolean[s.length];
        for (int i = 0; i < s.length; i++) {
            String str1 = s[i].trim();
            bools[i] = extractBoolean(str1);
        }
        return bools;
    }

    /**
     * Get the collection of the configuration item keys.
     *
     * @return collection of keys
     */
    public Enumeration<?> getKeys() {
        Enumeration<?> keys = props.propertyNames();
        return keys;
    }

    /**
     * Returns list of keys in the same order as they are written in the
     * configuration file.
     *
     * @return list of keys
     */
    public List<String> getKeyList() throws IOException {
        List<String> keys = new ArrayList<String>();
        Reader reader = (fileName == null) ? new InputStreamReader(inputStreamProvider.getInputStream()) : new FileReader(fileName);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#")) {
                int index = line.indexOf("=");
                String key = line.substring(0, index);
                keys.add(key);
            }
        }
        br.close();
        return keys;
    }

    /**
     * Saves the changes in the properties file.
     *
     * @throws IOException
     */
    public void save() throws IOException {
        File file = new File(fileName);
        OutputStream stream = new FileOutputStream(file);
        try {
            props.store(stream, null);
        } finally {
            stream.close();
        }
    }

    /**
     * Sets String value for the configuration item.
     *
     * @param key the configuration item key
     * @param value the value to be set
     */
    public void setString(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * Delete a configuration item.
     *
     * @param key the configuration item key
     */
    public void deleteString(String key) {
        props.remove(key);
    }

    /**
     * Checks the type of the value in the configuration file.
     *
     * @param type the required type
     * @param value value to be checked
     *
     * @return true iff the type check succeeded
     */
    public static boolean typeCheck(String type, String value) {
        if ("int".equals(type)) {
            try {
                extractInt(value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        if ("int[]".equals(type)) {
            try {
                extractIntArray(value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        if ("boolean".equals(type)) {
            try {
                extractBoolean(value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        if ("boolean[]".equals(type)) {
            try {
                extractBooleanArray(value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        if ("double".equals(type)) {
            try {
                extractDouble(value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        if ("String[]".equals(type)) {
            try {
                extractStringArray(value);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        if ("String".equals(type)) {
            return true;
        }
        return false;
    }
}
