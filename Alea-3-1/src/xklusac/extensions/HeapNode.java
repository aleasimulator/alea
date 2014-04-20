/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.extensions;

import java.util.ArrayList;

/**
 * Implements a node in Binary Heap that represent a list of CPUs with the same availability time.
 * @author Dalibor Klusacek
 */
public class HeapNode implements Comparable<HeapNode>{

    private double time = Double.MAX_VALUE;
    private ArrayList<Integer> cpuIDs = new ArrayList<Integer>();

    public HeapNode(double freetime, ArrayList<Integer> cpus) {
        this.setCpuIDs(cpus);
        this.setTime(freetime);        
    }

    /**
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }

    /**
     * @return the cpuIDs
     */
    public ArrayList<Integer> getCpuIDs() {        
        return cpuIDs;
    }

    /**
     * @return the cpuIDs
     */
    public ArrayList<Integer> getCpuIDs(int i) {
        //System.out.println("Retrieving "+cpuIDs.size()+" CPUids for job: "+i);
        return cpuIDs;
    }

    /**
     * @param cpuIDs the cpuIDs to set
     */
    public void setCpuIDs(ArrayList<Integer> cpuIDs) {
        this.cpuIDs = cpuIDs;
        //System.out.println("Updating CPUids in node - now = "+this.cpuIDs.size()+" cpu ids");
    }

    @Override
    public int compareTo(HeapNode thatNode) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        double thatTime = thatNode.getTime();

        
        if (this.getTime() < thatTime) {
            return BEFORE;
        }
        if (this.getTime() > thatTime) {
            return AFTER;
        }
        if (this.getTime() == thatTime) {
            return EQUAL;
        }

        return BEFORE;
    }
}
