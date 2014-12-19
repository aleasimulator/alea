package xklusac.extensions;
import java.util.Comparator;
import xklusac.environment.GridletInfo;
/**
 * Class StartComparator<p>
 * Compares two gridlets according to their start time.
 * @author Dalibor Klusacek
 */
public class StartComparator implements Comparator {
    
    /**
     * Compares two gridlets according to their start time
     */
    public int compare(Object o1, Object o2) {
        GridletInfo g1 = (GridletInfo) o1;
        GridletInfo g2 = (GridletInfo) o2;
        double priority1 = (Double) g1.getExpectedStartTime();
        double priority2 = (Double) g2.getExpectedStartTime();
        if(priority1 > priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 < priority2) return -1;
        return 0;
    }
    
}
