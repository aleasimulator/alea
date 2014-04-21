package xklusac.environment;

/**
 * Class ConfigurationWeb is used as an auxiliary class for the index.jsp.
 * Defines HTTP parameters and their values.
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
}
