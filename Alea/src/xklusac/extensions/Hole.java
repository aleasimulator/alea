/*
 * Hole.java
 *
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xklusac.extensions;

import xklusac.environment.GridletInfo;

/**
 * Class Hole<p>
 * Represents a Hole (Gap) which is used to store dynamic information concerning unused "space" in schedule, i.e. unused CPU time.
 * @author Dalibor Klusacek
 */
public class Hole {
    private double start;
    private double end;
    private double length;
    private double mips;
    private int size;
    private GridletInfo position;
    
    /**
     * Creates a new instance of Hole (Gap) which represents unused "space" in schedule, i.e. unused CPU time.
     */
    public Hole(double start, double end, double length, double mips, int size, GridletInfo position) {
        this.setStart(start);
        this.setEnd(end);
        this.setLength(length);
        this.setSize(size);
        this.setPosition(position);
        this.setMips(mips);
    }
    /** Getter method - when hole starts. */
    public double getStart() {
        return start;
    }
    /** Setter method - when hole starts. */
    public void setStart(double start) {
        this.start = start;
    }
    /** Getter method - when hole ends. */
    public double getEnd() {
        return end;
    }
    /** Setter method - when hole ends. */
    public void setEnd(double end) {
        this.end = end;
    }
    /** Getter method - what is the length of the hole. */
    public double getLength() {
        return length;
    }
    /** Setter method - what is the length of the hole. */
    public void setLength(double length) {
        this.length = length;
    }
    /** Getter method - what is the size (i.e., how much PEs are involved) of the hole. */
    public int getSize() {
        return size;
    }
    /** Setter method - what is the size (i.e., how much PEs are involved) of the hole. */
    public void setSize(int size) {
        this.size = size;
    }
    /** Getter method - which Gridlet is right behind this hole. */
    public GridletInfo getPosition() {
        return position;
    }
    /** Setter method - which Gridlet is right behind this hole. */
    public void setPosition(GridletInfo position) {
        this.position = position;
    }
    /** Getter method - how many MIPS are acumulated in this hole. */
    public double getMips() {
        return mips;
    }
    /** Setter method - how many MIPS are acumulated in this hole. */
    public void setMips(double mips) {
        this.mips = mips;
    }
    
}
