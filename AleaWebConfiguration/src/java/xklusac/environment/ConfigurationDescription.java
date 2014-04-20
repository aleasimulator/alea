package xklusac.environment;

/**
 * Class ConfigurationDescription is used for work with the description file for the web application.
 * @author Gabriela Podolnikova
 */
public class ConfigurationDescription {
    
    private final String type;
    private final String shortDescription;
    private final String additionalInf;
    
    /**
     * Splits the value of the description file.
     * The value contains several parts, the first is for the type of the variable,
     * the second is for the name of the variable, and the third is for the additional information
     * and it is optional.
     * @param descriptionValue the String that will be split
     */
    public ConfigurationDescription (String descriptionValue) {
        String[] parts = descriptionValue.split("\\|");
        type = parts[0];
        shortDescription = parts[1];
        String addInf = "";
        if (parts.length >= 3) {
            addInf = parts[2];
        }
        additionalInf = addInf;
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
    
}
