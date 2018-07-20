/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.extensions;

import xklusac.environment.GridletInfo;

/**
 *
 * @author daliborT470
 */
public class SchedulingEvent {
    private long sch_time = 0;
    private GridletInfo gi = null;
    private boolean start = true;
    private int tick = 0;
    private SchedulingEvent start_event = null;
    private int cpu_shift = 0;
    
    public SchedulingEvent(long time, int cpu_sh, GridletInfo gif, boolean type){
        this.setSch_time(time);
        this.setGi(gif);
        this.setStart(type);        
        this.setTick(0);
        this.setCpu_shift(cpu_sh);
    }
    public SchedulingEvent(long time, int cpu_sh, GridletInfo gif, boolean type, SchedulingEvent sev){
        this.setSch_time(time);
        this.setGi(gif);
        this.setStart(type);        
        this.setTick(0);
        this.start_event = sev;
        this.setCpu_shift(cpu_sh);
    }

    /**
     * @return the sch_time
     */
    public long getSch_time() {
        return sch_time;
    }

    /**
     * @param sch_time the sch_time to set
     */
    public void setSch_time(long sch_time) {
        this.sch_time = sch_time;
    }

    /**
     * @return the gi
     */
    public GridletInfo getGi() {
        return gi;
    }

    /**
     * @param gi the gi to set
     */
    public void setGi(GridletInfo gi) {
        this.gi = gi;
    }

    /**
     * @return the start
     */
    public boolean isStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(boolean start) {
        this.start = start;
    }

    /**
     * @return the tick
     */
    public int getTick() {
        return tick;
    }

    /**
     * @param tick the tick to set
     */
    public void setTick(int tick) {
        this.tick = tick;
    }

    /**
     * @return the start_event
     */
    public SchedulingEvent getStart_event() {
        return start_event;
    }

    /**
     * @param start_event the start_event to set
     */
    public void setStart_event(SchedulingEvent start_event) {
        this.start_event = start_event;
    }

    /**
     * @return the cpu_shift
     */
    public int getCpu_shift() {
        return cpu_shift;
    }

    /**
     * @param cpu_shift the cpu_shift to set
     */
    public void setCpu_shift(int cpu_shift) {
        this.cpu_shift = cpu_shift;
    }
 
    
    
    
}
