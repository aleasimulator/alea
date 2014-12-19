/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.queue_based;

import java.util.Date;
import gridsim.GridSim;
import java.util.Collections;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.extensions.DeadlineComparator;

/**
 * Class EDF<p>
 * Implements EDF (Earliest Deadline First) algorithm.
 * @author       Dalibor Klusacek
 */

public class EDF implements SchedulingPolicy {

    private Scheduler scheduler;

    public EDF(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        double runtime1 = new Date().getTime();
        Scheduler.queue.addLast(gi);
        Collections.sort(Scheduler.queue, new DeadlineComparator());
        //System.out.println(GridSim.clock()+": FCFS job received");
        Scheduler.runtime += (new Date().getTime() - runtime1);
        //System.out.println("New job has been received by EDF");
    }

    @Override
    public int selectJob() {
        //System.out.println("Selecting job by EDF...");
        int scheduled = 0;
        ResourceInfo r_cand = null;
        for (int i = 0; i < Scheduler.queue.size(); i++) {
            GridletInfo gi = (GridletInfo) Scheduler.queue.get(i);
            for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
                ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);

                if (Scheduler.isSuitable(ri, gi)  && ri.canExecuteNow(gi)) {

                    r_cand = ri;
                    break;
                }
            }
            if (r_cand != null) {
                gi = (GridletInfo) Scheduler.queue.remove(i);
                //System.err.println(gi.getID()+" PEs size = "+gi.PEs.size());
                r_cand.addGInfoInExec(gi);
                // set the resource ID for this gridletInfo (this is the final scheduling decision)
                gi.setResourceID(r_cand.resource.getResourceID());
                // tell the JSS where to send which gridlet
                scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                r_cand.is_ready = true;
                scheduled++;
                r_cand = null;
                i--;
                return scheduled;
            } else {
                return scheduled;
            }
        }

        return scheduled;
    }
}
