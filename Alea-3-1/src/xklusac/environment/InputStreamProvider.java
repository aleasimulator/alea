package xklusac.environment;

import java.io.InputStream;

/**
 * Interface for providing an InputStream.
 * 
 * @author Gabriela Podolnikova
 */
public interface InputStreamProvider {
    
    /**
     * Returns an InputStream. This method can be invoked repeatedly. Each invocation returns
     * a new instance of the InputStream containing the same data.
     * 
     * @return an InputStream instance
     */
    public InputStream getInputStream();
}
