/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.queue_based.multi_queue;

import java.util.Date;
import gridsim.GridSim;
import java.util.LinkedList;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class FCFS<p>
 * Implements FCFS algorithm.
 *
 * @author Dalibor Klusacek
 */
public class FCFS implements SchedulingPolicy {

    private Scheduler scheduler;

    public FCFS(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        double runtime1 = new Date().getTime();
        int index = Scheduler.all_queues_names.indexOf(gi.getQueue());
        if (index == -1 || ExperimentSetup.by_queue == false) {
            index = 0;
        }

        LinkedList queue = Scheduler.all_queues.get(index);
        queue.addLast(gi);
        Scheduler.runtime += (new Date().getTime() - runtime1);
        //System.out.println(gi.getID()+ " has been received by FCFS");
    }

    @Override
    public int selectJob() {
        //System.out.println("Selecting job by FCFS...");
        int scheduled = 0;
        ResourceInfo r_cand = null;
        for (int q = 0; q < Scheduler.all_queues.size(); q++) {
            Scheduler.queue = Scheduler.all_queues.get(q);
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
                    //System.err.println(gi.getID()+" PEs size = "+gi.PEs.size());
                    //scheduler.logJobSubmit(gi.getGridlet(), r_cand.resource.getResourceID());
                    r_cand.addGInfoInExec(gi);
                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(r_cand.resource.getResourceID());
                    scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                    r_cand.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    //System.out.println(gi.getID()+": sended on res="+r_cand.resource.getResourceName()+" resFree="+r_cand.getNumFreePE()+" req="+gi.getNumPE()+" prop="+gi.getProperties());
                    scheduled++;
                    r_cand = null;
                    i--;
                    return scheduled;
                } else {
                    return scheduled;
                }
            }
        }
        return scheduled;
    }
}
