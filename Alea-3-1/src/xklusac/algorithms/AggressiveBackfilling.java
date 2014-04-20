/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms;

import java.util.Date;
import gridsim.GridSim;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class AggressiveBackfilling<p>
 * This class implements multi-queue priority-based fair share using scheduling policy, similar to the algorithm applied in Czech NGI MetaCentrum.
 * @author       Dalibor Klusacek
 */
public class AggressiveBackfilling implements SchedulingPolicy {

    private Scheduler scheduler;

    public AggressiveBackfilling(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        double runtime1 = new Date().getTime();
        //System.out.println("New job has been received by AggressiveBackfilling");
        Scheduler.queue.addLast(gi);
        Scheduler.runtime += (new Date().getTime() - runtime1);

    }

    /** Aggressive Backfilling like algorithm. Backfill every possible job from the queue.
     *  No reservations are established for the previous (waiting) jobs.
     */
    @Override
    public int selectJob() {
        //System.out.println("Selecting job by AggressiveBackfilling...");
        ResourceInfo r_cand = null;
        int scheduled = 0;

        // we go through the whole queue
        for (int i = 0; i < Scheduler.queue.size(); i++) {

            GridletInfo gi = (GridletInfo) Scheduler.queue.get(i);

            for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
                ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
                if (Scheduler.isSuitable(ri, gi) && ri.canExecuteNow(gi)) {
                    r_cand = ri;
                    break;
                }
            }
            if (r_cand != null) {
                gi = (GridletInfo) Scheduler.queue.remove(i);
                r_cand.addGInfoInExec(gi);
                // set the resource ID for this gridletInfo (this is the final scheduling decision)
                gi.setResourceID(r_cand.resource.getResourceID());
                if (gi.getNumPE() > r_cand.resource.getNumPE()) {
                    System.out.println("Backfill error : " + gi.getID());
                    // tell the JSS where to send which gridlet
                    }
                scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                r_cand.is_ready = true;
                //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, Scheduler.GridletWasSent, gi);
                scheduled++;
                // we removed a job from position i so the next job is now on i
                // we have to decrease the counter otherwise we would skip a job due to i++ in for loop
                i--;
                r_cand = null;
                return scheduled;
            }

        }//we went through the whole queue
        return scheduled;

    }
}
