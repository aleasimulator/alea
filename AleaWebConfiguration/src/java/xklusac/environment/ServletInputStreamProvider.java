package xklusac.environment;

import java.io.InputStream;
import javax.servlet.ServletContext;

/**
 * Class ServletInputStreamProvider implements the method of InputStreanProvider class.
 * It provides the input stream for the web application for the Alea configuration.
 * @author Gabriela Podolnikova
 */
public class ServletInputStreamProvider implements InputStreamProvider {

    private final String fileName;
    private final ServletContext servletContext;
    
    /**
     * Creates new instance of ServletInputStreamProvider.
     * @param fileName the name of the file to be loaded
     * @param servletContext the context for the web application
     */
    public ServletInputStreamProvider(String fileName, ServletContext servletContext) {
        this.fileName = fileName;
        this.servletContext = servletContext;
    } 
    
    /**
     * @see InputStreamProvider 
     */
    @Override
    public InputStream getInputStream() {
        return servletContext.getResourceAsStream(fileName);
    }
    
}
