package xklusac.environment;

import java.io.InputStream;

/**
 * Interface InputStreamProvider defines the method that the ServletInputStreamProvider should implement.
 * @author Gabriela Podolnikova
 */
public interface InputStreamProvider {
    
    /**
     * Returns the stream of a file in the given context in the web application.
     * @return the resource as stream
     */
    public InputStream getInputStream();
}
