/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xklusac.extensions;

import java.util.Comparator;

/**
 * Simple comparator class, which sort holes by their start time.
 * Used by schedule based algorithms.
 *
 * @author Jiri Oliva
 */
public class StartTimeHoleComparator implements Comparator {
    
    public int compare(Object o1, Object o2) {
        Hole h1 = (Hole) o1;
        Hole h2 = (Hole) o2;
        double priority1 = (Double) h1.getStart();
        double priority2 = (Double) h2.getStart();
        if(priority1 > priority2) return 1;
        if(priority1 == priority2) return 0;
        if(priority1 < priority2) return -1;
        return 0;
    }
    
}