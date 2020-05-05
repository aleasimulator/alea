/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.queue_based.multi_queue;

import gridsim.GridSim;
import java.util.Date;
import java.util.Collections;
import java.util.LinkedList;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.extensions.ArrivalComparator;
import xklusac.extensions.WallclockComparator;

/**
 * Class AggressiveBackfilling<p>
 * This class implements multi-queue priority-based fair share using scheduling
 * policy, similar to the algorithm applied in Czech NGI MetaCentrum.
 *
 * @author Dalibor Klusacek
 */
public class AggressiveBackfillingFastDeepHold implements SchedulingPolicy {

    private Scheduler scheduler;

    public AggressiveBackfillingFastDeepHold(Scheduler scheduler) {
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
        //System.out.println(gi.getID()+" New job is in queue: "+gi.getQueue()+" index "+index);
        Scheduler.runtime += (new Date().getTime() - runtime1);

    }

    /**
     * Aggressive Backfilling like algorithm. Backfill every possible job from
     * the queue. No reservations are established for the previous (waiting)
     * jobs.
     */
    @Override
    public int selectJob() {
        //System.out.println("Selecting job by AggressiveBackfilling...");
        ResourceInfo r_cand = null;
        int scheduled = 0;
        int max_free_CPUs = 0;
        LinkedList<ResourceInfo> eligible_resources = new LinkedList();
        for (int e = 0; e < Scheduler.resourceInfoList.size(); e++) {
            ResourceInfo re = (ResourceInfo) Scheduler.resourceInfoList.get(e);
            int frp = re.getNumFreePE();
            if (frp > 0) {
                eligible_resources.add(re);
                if (frp > max_free_CPUs) {
                    max_free_CPUs = frp;
                }
            }
        }

        for (int q = 0; q < Scheduler.all_queues.size(); q++) {
            //System.out.println(q+" select queue "+Scheduler.all_queues_names.get(q) );
            Scheduler.queue = Scheduler.all_queues.get(q);
            if (ExperimentSetup.use_fairshare) {
                System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
                Collections.sort(Scheduler.queue, new WallclockComparator());
            } else if (ExperimentSetup.extract_jobs) {
                System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
                Collections.sort(Scheduler.queue, new ArrivalComparator());
            }

            if (max_free_CPUs < 1) {
                Scheduler.tried_queue.addAll(Scheduler.queue);
                Scheduler.queue.clear();
                return 0;
            }

            // we go through the whole queue
            while (Scheduler.queue.size() > 0) {

                GridletInfo gi = find_first_eligible_job(Scheduler.queue, max_free_CPUs);
                if (gi == null) {
                    //System.out.println(Math.round(GridSim.clock())+" -no eligible job- : waiting in this queue("+q+"): "+Scheduler.queue.size()+ " of "+Scheduler.getQueueSize()+", free CPUs "+max_free_CPUs);
                    break;
                }

                for (int j = 0; j < eligible_resources.size(); j++) {
                    ResourceInfo ri = (ResourceInfo) eligible_resources.get(j);
                    if (Scheduler.isSuitable(ri, gi) && ri.canExecuteNow(gi)) {
                        r_cand = ri;
                        break;
                    }
                }
                if (r_cand != null) {
                    gi = (GridletInfo) Scheduler.queue.remove(Scheduler.queue.indexOf(gi));
                    r_cand.addGInfoInExec(gi);
                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(r_cand.resource.getResourceID());

                    scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                    r_cand.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_Job_Scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                    // we removed a job from position i so the next job is now on i
                    // we have to decrease the counter otherwise we would skip a job due to i++ in for loop

                    r_cand = null;
                    return scheduled;
                } else {
                    Scheduler.tried_queue.add(gi);
                    Scheduler.queue.remove(gi);
                    //System.out.println(gi.getID()+" added to tried queue for now (will return) as it cannot run now.");
                }

            }//we went through the whole queue
        }//next queue
        eligible_resources = null;
        return scheduled;

    }

    private GridletInfo find_first_eligible_job(LinkedList<GridletInfo> queue, int max_free_cpu_res) {

        for (int i = 0; i < queue.size(); i++) {
            GridletInfo gi = queue.get(i);

            int quota_free = Integer.MAX_VALUE;
            // prvni verze mela tento backfill trochu
            // if (quota_free >= gi.getNumPE() && gi.getNumPE() <= max_free_cpu_res) {
            if (gi.getNumPE() <= max_free_cpu_res) {
                //if(i>0){
                //System.out.println(i+"th job "+gi.getID()+" from queue selected. Requires "+gi.getNumPE()+" CPU and avail: "+max_free_cpu_res+" req RAM = "+(gi.getRam()/(1024.0*1024))+" GB at time:"+GridSim.clock());
                //System.out.println();
                //}
                return gi;
                //pridam rozdvojku (v puvodni verzi neni)
                /*if (gi.getNumPE() <= max_free_cpu_res) {
                    return gi;
                } else {
                    //System.out.print("job too large ("+gi.getNumPE()+" cpus) ");
                    return null;
                }*/
            } else {
                Scheduler.tried_queue.add(gi);
                queue.remove(i);
                //System.out.println(gi.getID()+": moved to hold queue, group free= "+g.getFreeQuota()+" job req= "+gi.getNumPE()+" removed from position= "+i);
                i--;
                //System.out.println(gi.getID()+": Quota limit exceeded: "+quota_free+" avail CPUs of limit: "+g.getQuota()+" group "+g.getName());
                //System.out.println(gi.getID()+": group free "+ExperimentSetup.groups.get(gi.getGroup()).getFreeQuota() +" of "+ExperimentSetup.groups.get(gi.getGroup()).getQuota()+" req: "+gi.getNumPE());
            }
            if (max_free_cpu_res < 11 && i > 1000) {
                //System.out.print("queue depth limit ");
                return null;
            }

        }
        //System.out.print("whole queue (weird) ");
        return null;
    }

}
