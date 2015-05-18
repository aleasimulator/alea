package xklusac.environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Class ConfigurationDescription is used for work with the description file for the web application.
 * 
 * @author Gabriela Podolnikova
 */
public class ConfigurationDescription {
    
    private final String type;
    private final String shortDescription;
    private final String additionalInf;
    private final String alg;
    
    /**
     * Splits the value of the description item.
     * The value contains several parts, the first one is the type of the variable,
     * the second one is the name of the variable, and the third one is the additional information
     * and it is optional.
     * 
     * @param descriptionValue the String that will be split
     */
    public ConfigurationDescription (String descriptionValue) {
        descriptionValue = descriptionValue.replace("||", "| |");
        String[] parts = descriptionValue.split("\\|");
        type = parts[0];
        shortDescription = parts[1];
        additionalInf = parts[2];
        String algorithm = "";
        if (parts.length >= 4) {
            algorithm = parts[3];
        }
        alg = algorithm;
    }
    
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @return the additionalInf
     */
    public String getAdditionalInf() {
        return additionalInf;
    }

    /**
     * @return the alg
     */
    public List<Integer> getAlg() {
        List<Integer> algorithms = new ArrayList<>();
        if (!alg.isEmpty()) {
            String[] algParts = alg.split(",");
            for (String part : algParts) {
                algorithms.add(Integer.parseInt(part));
            }
        }
        return algorithms;
    }
}
