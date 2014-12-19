/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based;

import java.util.Date;
import gridsim.GridSim;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.objective_functions.CommonObjectives;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class BestGap <p>
 * Contains implementation of schedule-based Best Gap policy which works similarly as Conservative Backfilling.
 * @author       Dalibor Klusacek
 */
public class BestGap implements SchedulingPolicy {

    private Scheduler scheduler;

    public BestGap(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        int index = 0;
        int resIndex = -2;
        int gIndex = -1;
        double current_time = GridSim.clock();
        double runtime1 = new Date().getTime();


        double previous_resp = CommonObjectives.predictAvgResponseTime(current_time) + 100000;
        double previous_wait = CommonObjectives.predictAvgWaitTime(current_time) + 100000;
        double previous_sd = CommonObjectives.predictAvgSlowdown(current_time) + 100000;
        double previous_fair = CommonObjectives.predictFairness(current_time) + 100000;
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
                double new_decision = 0.0;

                double current_fair = CommonObjectives.predictFairness(current_time);
                double fair = Math.max(0.0000000000001, previous_fair);
                double diff_fair = (previous_fair - current_fair) / fair;

                double current_resp = CommonObjectives.predictAvgResponseTime(current_time);
                double rt = Math.max(0.0000000000001, previous_resp);
                double diff_rt = (previous_resp - current_resp) / rt;

                double current_wait = CommonObjectives.predictAvgWaitTime(current_time);
                double wt = Math.max(0.0000000000001, previous_wait);
                double diff_wt = (previous_wait - current_wait) / wt;

                double current_sd = CommonObjectives.predictAvgSlowdown(current_time);
                double sd = Math.max(1.0, previous_sd);
                double diff_sd = (previous_sd - current_sd) / sd;


                // decision taken upon slowdown, response time a wait time values
                new_decision = (diff_fair * ExperimentSetup.fair_weight) + (diff_rt * 1.0) + (diff_wt * 1.0) + (diff_sd * 1.0);

                if (new_decision <= 0.0 && accept == false) {
                    //bad move
                    ri.removeGInfo(gi);
                    ResourceInfo rPrev = (ResourceInfo) Scheduler.resourceInfoList.get(resIndex);
                    rPrev.addGInfo(gIndex, gi);

                } else {
                    // good move
                    accept = false;
                    previous_sd = current_sd;
                    previous_wait = current_wait;
                    previous_resp = current_resp;
                    previous_fair = current_fair;
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
        //System.out.println("New job has been received by BestGap");

    }

    @Override
    public int selectJob() {
        //System.out.println("Selecting job by Best Gap...");
        int scheduled = 0;
        for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
            if (ri.resSchedule.size() > 0) {
                GridletInfo gi = (GridletInfo) ri.resSchedule.get(0);
                if (ri.canExecuteNow(gi)) {
                    ri.removeFirstGI();
                    ri.addGInfoInExec(gi);
                    //System.out.println(Math.round(GridSim.clock()) + ": send gi "+gi.getID()+" on "+GridSim.getEntityName(ri.resource.getResourceID())+" req/free = "+gi.getNumPE()+"/"+free);

                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(ri.resource.getResourceID());
                    // tell the user where to send which gridlet

                    scheduler.submitJob(gi.getGridlet(), ri.resource.getResourceID());

                    ri.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                }
            }
        }
        return scheduled;
    }
}
