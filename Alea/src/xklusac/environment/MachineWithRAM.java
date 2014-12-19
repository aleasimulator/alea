/*
 * Title:        GridSim Toolkit
 * Description:  GridSim (Grid Simulation) Toolkit for Modeling and Simulation
 *               of Parallel and Distributed Systems such as Clusters and Grids
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * $Id: Machine.java,v 1.14 2007/08/20 02:13:29 anthony Exp $
 */
package xklusac.environment;

import java.util.Iterator;
import gridsim.*;
import java.util.Arrays;

/**
 * GridSim Machine class represents an uniprocessor or shared memory
 * multiprocessor machine. It can contain one or more Processing Elements (PEs).
 *
 * @author Manzur Murshed and Rajkumar Buyya
 * @since GridSim Toolkit 1.0 @invariant $none
 */
public class MachineWithRAM extends Machine {
    // |PEs| > 1 is SMP (Shared Memory Multiprocessors)    

    private long ram;
    private long used_ram;
    private int freeVirtualPE;
    private double[] firstFreeTime;
    // tells whether this machine is working properly or has failed.
    private boolean failed_;

    /**
     * Allocates a new Machine object
     *
     * @param id the machine ID
     * @param list list of PEs @pre id > 0 @pre list != null @post $none
     */
    public MachineWithRAM(int id, PEList list, long ram) {
        super(id, list);
        this.ram = ram;
        this.used_ram = 0;
        this.firstFreeTime = new double[list.size()];
        for (int i = 0; i < firstFreeTime.length; i++) {
            firstFreeTime[i] = 0.0;
        }
    }

    /**
     * @return the ram
     */
    public long getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(long ram) {
        this.ram = ram;
    }

    /**
     * @return the used_ram
     */
    public long getUsedRam() {
        return used_ram;
    }

    /**
     * @param used_ram the used_ram to set
     */
    public void setUsedRam(long used_ram) {
        this.used_ram = used_ram;
    }

    /**
     * @return the free ram memory
     */
    public long getFreeRam() {
        return Math.max(0, (this.getRam() - this.getUsedRam()));
    }

    /**
     * @return the free ram memory
     */
    public double getPercUsedRam() {
        return Math.max(0, (((this.getUsedRam() * 1.0) / this.getRam())) * 100);
    }

    /**
     * @return the freeVirtualPE
     */
    public int getNumFreeVirtualPE() {
        return freeVirtualPE;
    }

    /**
     * @param freeVirtualPE the freeVirtualPE to set
     */
    public void setNumFreeVirtualPE(int freeVirtualPE) {
        this.freeVirtualPE = freeVirtualPE;
    }

    /**
     * @return the firstFreeTime
     */
    public double getFirstFreeTimeOnPE(int index) {

        return firstFreeTime[index];
    }

    public double[] getFirstFreeTimeArray() {
        return firstFreeTime;
    }

    public void setFirstFreeTimeArray(double[] a) {
        firstFreeTime = a;
    }

    /**
     * @param firstFreeTime the firstFreeTime to set
     */
    public void setFirstFreeTimeOnPE(int index, double firstFreeTime) {
        //System.out.println("EST on machine "+this.getMachineID()+" | "+firstFreeTime);
        this.firstFreeTime[index] = firstFreeTime;
    }

    public void updateFirstFreeTimeAfterNodeJobAllocation(int ppn, double runtime) {
        //System.out.println("EST on machine "+this.getMachineID()+" | "+firstFreeTime);
        double est = -1.0;
        for (int p = 0; p < ppn; p++) {
            double min = Double.MAX_VALUE;
            int index = -1;
            for (int i = 0; i < firstFreeTime.length; i++) {
                if (firstFreeTime[i] <= min && firstFreeTime[i] > -998) {
                    index = i;
                    min = firstFreeTime[i];
                }
            }
            if (p != ppn - 1) {
                firstFreeTime[index] = -999;
            } else {
                firstFreeTime[index] = Double.MAX_VALUE; // TO DO prekopat v budoucnu
                est = firstFreeTime[index];
            }
        }
        // oznacene uzly nastavim na est + time
        for (int i = 0; i < firstFreeTime.length; i++) {
            if (firstFreeTime[i] < -998) {
                firstFreeTime[i] = est;
            }
        }
        
        /*for (int i = 0; i < firstFreeTime.length; i++) {
            System.out.print(Math.round(firstFreeTime[i])+",");
        }
        System.out.println("| Max updated...:");*/
    }

    public double getEarliestStartTimeForNodeJob(int ppn) {
        double est = Double.MAX_VALUE;
        if (ppn > this.getNumPE()) {
            return est;
        }
        double[] est_times = Arrays.copyOf(firstFreeTime, firstFreeTime.length);
        Arrays.sort(est_times);


        /*System.out.print(Math.round(GridSim.clock()) + " | machine " + this.getMachineID() + " |");
        for (int i = 0; i < est_times.length;
                i++) {
            System.out.print(Math.round(est_times[i]) + ",");
        }
        System.out.println("| EST = " + Math.round(est_times[ppn - 1]) + " for PPN          = " + ppn);
        */

        return est_times[ppn - 1];
    }
} // end class

