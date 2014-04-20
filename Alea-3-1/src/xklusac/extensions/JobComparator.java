/*
 * JobComparator.java
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
 * Class JobComparator<p>
 * Compares two gridlets wrt. their priority
 * @author Dalibor Klusacek
 */
public class JobComparator implements Comparator {
    
    /**
     * Compares two gridlets according to their priority
     */
    public int compare(Object o1, Object o2) {
        GridletInfo g1 = (GridletInfo) o1;
        GridletInfo g2 = (GridletInfo) o2;
        double priority1 = (Double) g1.getPriority();
        double priority2 = (Double) g2.getPriority();
        if(priority1 < priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 > priority2) return -1;
        return 0;
    }
    
}
