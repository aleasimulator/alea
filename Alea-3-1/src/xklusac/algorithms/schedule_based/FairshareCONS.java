/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based;

import java.util.Date;
import gridsim.GridSim;
import java.util.Collections;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.extensions.WallclockComparator;

/**
 * Class CONS<p> Implements CONS (Conservative Backfilling).
 *
 * @author Dalibor Klusacek
 */
public class FairshareCONS implements SchedulingPolicy {

    private Scheduler scheduler;

    public FairshareCONS(Scheduler scheduler) {
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
            if (ri.findHoleForGridlet(gi)) {
                okh = true;
                index = ri.resSchedule.indexOf(gi);
                evaluate = true;

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

                // test the new assignement
                if (start_time_new >= best_start_time && accept == false) {
                    //bad move
                    ri.removeGInfo(gi);
                    ResourceInfo rPrev = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
                    rPrev.addGInfo(gIndex, gi);

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
        // updates resource info's internal values (IMPORTANT! because of next use of this policy)
        ri.forceUpdate(GridSim.clock());
        //System.out.println("New job has been received by CONS");

    }

    @Override
    public int selectJob() {

        // reinsert jobs according to current fairshare
        ResourceInfo ri = null;

        if (ExperimentSetup.use_fairshare) {

            // remove jobs, resort jobs via fairshare priority
            for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
                ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
                //System.out.println(i+": Starting fairshare update of schedule of " + ri.resSchedule.size() + " jobs.");
                Scheduler.schedQueue2.addAll(ri.resSchedule);
                ri.resSchedule.clear();
                ri.stable = false;
                ri.holes.clear();
            }
            Collections.sort(Scheduler.schedQueue2, new WallclockComparator());


            // reinsert jobs using CONS
            for (int i = 0; i < Scheduler.schedQueue2.size(); i++) {
                //System.out.print(((GridletInfo) Scheduler.schedQueue2.get(i)).getUser()+"("+Math.round(((GridletInfo) Scheduler.schedQueue2.get(i)).getPriority())+"),");
                addNewJob((GridletInfo) Scheduler.schedQueue2.get(i));
            }
            //System.out.println("---EOF");
            Scheduler.schedQueue2.clear();

        }
        //System.out.println("Selecting job by CONS...");
        int scheduled = 0;
        for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
            ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
            if (ri.resSchedule.size() > 0) {
                GridletInfo gi = (GridletInfo) ri.resSchedule.get(0);
                if (ri.canExecuteNow(gi)) {
                    ri.removeFirstGI();
                    ri.addGInfoInExec(gi);


                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(ri.resource.getResourceID());
                    // tell the user where to send which gridlet

                    scheduler.submitJob(gi.getGridlet(), ri.resource.getResourceID());

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
