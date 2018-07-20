package xklusac.extensions;
import java.util.Comparator;
/**
 * Class StartComparator<p>
 * Compares two gridlets according to their start time.
 * @author Dalibor Klusacek
 */
public class EndTimeComparator implements Comparator {
    
    /**
     * Compares two gridlets according to their start time
     */
    public int compare(Object o1, Object o2) {
        SchedulingEvent g1 = (SchedulingEvent) o1;
        SchedulingEvent g2 = (SchedulingEvent) o2;
        double priority1 = g1.getSch_time()*1.0;
        double priority2 = g2.getSch_time()*1.0;
        if(priority1 > priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 < priority2) return -1;
        return 0;
    }
    
}
