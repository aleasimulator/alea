/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based;

import java.util.Date;
import gridsim.GridSim;
import java.util.ArrayList;
import java.util.Collections;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.extensions.Schedule_Visualizator;
import xklusac.extensions.EndTimeComparator;
import xklusac.extensions.SchedulingEvent;

/**
 * Class FCFS_Plan<p>
 * Implements FCFS-like scheduling policy, that creates jobs schedule using FCFS. 
 * No backfilling is used to avoid changes in node allocations of already planned jobs.
 *
 * @author Dalibor Klusacek
 */
public class FCFS_Plan implements SchedulingPolicy {

    private Scheduler scheduler;
    Schedule_Visualizator anim = null;

    public FCFS_Plan(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        int index = 0;
        int resIndex = -2;
        int gIndex = -1;
        double current_time = GridSim.clock();
        double runtime1 = new Date().getTime();
        double best_start_time = Double.MAX_VALUE;
        boolean accept = true;
        boolean ok = false;
        boolean okh = false;

        // select schedule with earliest suitable gap
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);

            boolean evaluate = false;

            // continue when not suitable.
            if (!Scheduler.isSuitableThenUpdate(ri, gi, current_time)) {
                continue;
            }
            ok = true;
            // if the gap is found - use it
            int gindex = ri.resSchedule.size();
            if (ri.addGInfo(gindex, gi)) {
                okh = true;
                index = ri.resSchedule.indexOf(gi);
                evaluate = true;
                //gi.setResourceID(ri.resource.getResourceID());

                // exists previous assignement
                if (resIndex >= 0) {
                    ResourceInfo rPrev = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
                    boolean odstr = rPrev.removeGInfo(gi);
                }
            } else {
                evaluate = false;
            }
            // if the move was made - evaluate this solution
            if (evaluate) {

                Scheduler.updateResourceInfos(current_time);
                double start_time_new = gi.getExpectedStartTime();
                //System.out.println(gi.getID()+" considers res "+ri.resource.getResourceName()+ "  start = "+start_time_new+" time:"+GridSim.clock());

                // test the new assignement
                if (start_time_new >= best_start_time && accept == false) {
                    //bad move
                    ri.removeGInfo(gi);
                    ResourceInfo rPrev = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
                    rPrev.addGInfo(gIndex, gi);
                    //gi.setResourceID(rPrev.resource.getResourceID());

                } else {
                    // good move
                    accept = false;
                    best_start_time = start_time_new;
                    resIndex = i;
                    gIndex = index;
                    gi.setResourceID(ri.resource.getResourceID());
                }
            }
        }
        Scheduler.runtime += (new Date().getTime() - runtime1);
        if (!Scheduler.isExecutable(gi)) {
            System.out.println(gi.getID() + " is not executable - danger!!! ok=" + ok + " hole=" + okh);
        }
        ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);

        // mark job as backfilled if it is not at the end of schedule
        int gi_index = ri.resSchedule.indexOf(gi);
        if (gi_index != (ri.resSchedule.size() - 1)) {
            ExperimentSetup.backfilled++;
        }
        // updates resource info's internal values (IMPORTANT! because of next use of this policy)
        ri.forceUpdate(GridSim.clock());

        // different backfill computation
        double g_start = gi.getExpectedFinishTime();
        for (int b = 0; b < ri.resSchedule.size(); b++) {
            GridletInfo gs = ri.resSchedule.get(b);
            if (gs.getExpectedStartTime() > g_start) {
                ExperimentSetup.backfilled_cons++;
                //System.out.println(gi.getID() + " backfilled! ");
                break;
            }
        }
        // sets the first predicted wait (which can change in the future)
        gi.getGridlet().setPredicted_wait(gi.getExpectedWaitTime());

        //System.out.println(gi.getID()+": New job has been received by CONS");
        if (ExperimentSetup.visualize_schedule) {
            anim = ExperimentSetup.schedule_windows.get(0);
            ArrayList[] schedules = new ArrayList[Scheduler.resourceInfoList.size()];
            int cpu_shift = 0;
            for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
                ResourceInfo r = (ResourceInfo) Scheduler.resourceInfoList.get(i);

                ArrayList<SchedulingEvent> job_schedule = new ArrayList();
                for (int s = 0; s < r.resSchedule.size(); s++) {
                    GridletInfo ginf = r.resSchedule.get(s);
                    //System.out.println(i+": "+ginf.getID()+" CPU-shift:"+cpu_shift+" cluster "+r.resource.getResourceName());
                    SchedulingEvent job_start = new SchedulingEvent(Math.round(ginf.getExpectedStartTime()), cpu_shift, ginf, true);
                    job_schedule.add(job_start);
                    job_schedule.add(new SchedulingEvent(Math.round(ginf.getExpectedFinishTime()), cpu_shift, ginf, false, job_start));
                }
                //sort all scheduling events by their time
                Collections.sort(job_schedule, new EndTimeComparator());
                schedules[i] = job_schedule;
                //System.out.println("-----------------------------CPU-shift:"+cpu_shift);
                cpu_shift += r.getNumRunningPE();

            }
            anim.reDrawSchedule(schedules, Scheduler.resourceInfoList.size(), scheduler.cl_names, scheduler.cl_CPUs);
            try {
                /*if(gi.getID()==141){
                    Thread.sleep(5000);
                }else{*/
                Thread.sleep(ExperimentSetup.schedule_repaint_delay);
                //}
            } catch (InterruptedException e) {
            }

        }
    }

    @Override
    public int selectJob() {
        //System.out.println("Selecting job by CONS...");
        int scheduled = 0;
        for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
            if (ri.resSchedule.size() > 0) {
                GridletInfo gi = (GridletInfo) ri.resSchedule.get(0);
                if (ri.canExecuteNow(gi)) {
                    ri.removeFirstGI();
                    ri.addGInfoInExec(gi);

                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(ri.resource.getResourceID());
                    // tell the user where to send which gridlet

                    scheduler.submitJob(gi.getGridlet(), ri.resource.getResourceID());
                    /*if(ri.resource.getResourceName().equals("fat")){
                        System.out.println(gi.getID() + " ................. will run at: " +ri.resource.getResourceName());
                    }*/
                    //System.out.println(gi.getID() + " is starting at: "+GridSim.clock()+", will finish at: " + (GridSim.clock() + gi.getJobRuntime(1)) + " runtime=" + gi.getJobRuntime(1) + " exp-finish-time=" + gi.getExpectedFinishTime() + " plannedPEs:" + gi.getPlannedPEsString());

                    ri.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                    return scheduled;
                }
            }
        }
        return scheduled;
    }
}
