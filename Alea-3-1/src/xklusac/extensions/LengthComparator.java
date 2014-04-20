/*
 * LengthComparator.java
 *
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xklusac.extensions;
import java.util.Comparator;
import xklusac.environment.GridletInfo;

/**
 * Class LengthComparator<p>
 * Compares two gridlets according to their estimated length
 * @author Dalibor Klusacek
 */
public class LengthComparator  implements Comparator {
    
    /**
     * Compares two gridlets according to their estimated length
     */
    public int compare(Object o1, Object o2) {
        GridletInfo g1 = (GridletInfo) o1;
        GridletInfo g2 = (GridletInfo) o2;
        double length1 = (Double) g1.getEstimatedLength();
        double length2 = (Double) g2.getEstimatedLength();
        if(length1 > length2) return 1;
        if(length1 == length2) return 0;
        if(length1 < length2) return -1;
        return 0;
    }
    
}
