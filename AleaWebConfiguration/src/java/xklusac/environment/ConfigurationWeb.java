package xklusac.environment;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;

/**
 * Class ConfigurationWeb is used as an auxiliary class for the index.jsp.
 * Defines HTTP parameters and their values. Also provides routine for
 * loading the Alea configuration file.
 *
 * @author Gabriela Podolnikova
 */
public class ConfigurationWeb {

    public static class Parameters {

        public static final String ACFG = "acfg_";
        public static final String DEL = "del_";
        public static final String ADD = "add_";
        public static final String DEL_ACFG = DEL + ACFG;
        public static final String ADDED_KEY = "addedKey";
        public static final String ADDED_VALUE = "addedValue";
    }

    public static class Values {

        public static final String OK = "OK";
        public static final String DEFAULTS = "Defaults";
        public static final String DEL = "DEL";
        public static final String ADD = "ADD";
    }

    public static AleaConfiguration getAleaConfiguration(ServletContext ctx) {
        AleaConfiguration aCfg = null;
        String aleaHome = System.getenv("ALEA_HOME");
        if (aleaHome != null) {
            try {
                aCfg = new AleaConfiguration(aleaHome + File.separator + "configuration.properties");
            } catch (IOException e) {
            }
        } else {
            try {
                InputStreamProvider inputStreamProviderForConfiguration = new ServletInputStreamProvider("/WEB-INF/configuration.properties", ctx);
                aCfg = new AleaConfiguration(inputStreamProviderForConfiguration);
            } catch (Exception e) {
            }
        }
        return aCfg;
    }
}
