package xklusac.environment;

import java.io.InputStream;
import javax.servlet.ServletContext;

/**
 * Class ServletInputStreamProvider implements the InputStreanProvider class.
 * It provides the input stream for the given File and ServletContext.
 * 
 * It can be used to provide the input for AleaConfiguration.
 * 
 * @author Gabriela Podolnikova
 */
public class ServletInputStreamProvider implements InputStreamProvider {

    private final String fileName;
    private final ServletContext servletContext;
    
    /**
     * Creates a new instance of ServletInputStreamProvider.
     * 
     * @param fileName the name of the file to be loaded
     * @param servletContext the context of the web application
     */
    public ServletInputStreamProvider(String fileName, ServletContext servletContext) {
        this.fileName = fileName;
        this.servletContext = servletContext;
    } 
    
    /**
     * @see InputStreamProvider#getInputStream() 
     */
    @Override
    public InputStream getInputStream() {
        return servletContext.getResourceAsStream(fileName);
    }
    
}
