/*
 * Hole.java
 *
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xklusac.extensions;

import java.util.LinkedList;
import xklusac.environment.GridletInfo;
import xklusac.environment.MachineWithRAM;

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
     private long ram;
    private int machineID;
    private boolean infinityLastGap = false;
    private double maxEnd;
    
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
    
    /**
     * Used when planning with RAM requirements.
     * Creates a new instance of Hole (Gap) which represents unused "space" in
     * schedule, i.e. unused CPU time.
     */
    public Hole(double start, double end, double length, double mips, Integer cpus, long ram, GridletInfo position, int machineID) {
        this.setStart(start);
        this.setEnd(end);
        this.setLength(length);
        this.setSize(cpus);
        this.setPosition(position);
        this.setMips(mips);
        this.setRam(ram);
        this.setMachineID(machineID);
        this.setMaxEnd(start);

    }
    
    /**
     * Creates infinity last hole for machine in given time
     * 
     * @param startTime
     * @param machine
     * @return new infinity Hole
     */
    public static Hole createLastInfiniteHole(double startTime, MachineWithRAM machine) {
        Hole h = new Hole(startTime, 0, 0, 0, machine.getNumPE(), machine.getRam(), null, machine.getMachineID());
        h.setInfinityLastGap(true);
        return h;
    }

    /**
     * Create deep copy of this Hole.
     * 
     * @return copied Hole
     */
    public Hole createCopy() {
        Hole copy = new Hole(start, end, length, mips, size, ram, position, machineID);
        return copy;
    }

    /**
     * Tests if this hole is large enough and has resources for given gridlet.
     * 
     * @param Gridlet
     * @param calculated length of the gridlet
     * @return true if suitable
     */
    public boolean isSuitableForGridlet(GridletInfo gi, Double jobMIPS) {
        if (infinityLastGap == true) {
            return true;
        } else {
            if (size >= gi.getPpn() && ram >= gi.getRam() && mips >= jobMIPS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if this hole has enough resources for given gridlet, but has not length
     * 
     * @param Gridlet
     * @param calculated length of the gridlet
     * @return true if hole has enough resources, but not length
     */
    public boolean isShortForGridlet(GridletInfo gi, Double jobMIPS) {

        return (size >= gi.getPpn() && ram >= gi.getRam() && mips < jobMIPS);
    }

    /**
     * Tests if this hole has enough resources for given gridlet
     * 
     * @param Gridlet
     * @return true if hole has enough resources
     */
    public boolean hasEnoughSourcesForGridlet(GridletInfo gi) {

        return (size >= gi.getPpn() && ram >= gi.getRam());
    }

    /**
     * Update length and MIPS of this hole.
     * 
     * @param peRating of ResourceInfo
     */
    public void calculateLengthAndMIPS(Integer peRating) {
        length = end - start;
        mips = length * peRating;
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
    /** Getter method - amount of RAM */
    public long getRam() {
        return ram;
    }
    /** Setter method - amount of RAM */
    public void setRam(long ram) {
        this.ram = ram;
    }
    /** Getter method - machineID of this hole */
    public int getMachineID() {
        return machineID;
    }
    /** Setter method - machineID of this hole */
    public void setMachineID(int machineID) {
        this.machineID = machineID;
    }
    /** Getter method - flag if this hole is last gap */
    public boolean isInfinityLastGap() {
        return infinityLastGap;
    }
    /** Setter method - flag if this hole is last gap */
    public void setInfinityLastGap(boolean infinityLastGap) {
        this.infinityLastGap = infinityLastGap;
    }
    /** Getter method - maximal finish time with following holes */
    public double getMaxEnd() {
        return maxEnd;
    }
    /** Setter method - maximal finish time with following holes */
    public void setMaxEnd(double maxEnd) {
        this.maxEnd = maxEnd;
    }
    
}
