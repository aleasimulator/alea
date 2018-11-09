package xklusac.extensions;
import java.util.Comparator;
import xklusac.environment.GridletInfo;
/**
 * Class ProcessorComparator<p>
 * Compares two CPUs according to their ID.
 * @author Dalibor Klusacek
 */
public class ProcessorComparator implements Comparator {
    
    /**
     * Compares two gridlets according to their start time
     */
    public int compare(Object o1, Object o2) {
        Integer g1 = (Integer) o1;
        Integer g2 = (Integer) o2;
        Integer priority1 = g1;
        Integer priority2 = g2;
        if(priority1 > priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 < priority2) return -1;
        return 0;
    }
    
}
