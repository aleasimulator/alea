/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.queue_based;

import java.util.Date;
import gridsim.GridSim;
import java.util.Collections;
import java.util.LinkedList;
import xklusac.algorithms.SchedulingPolicy;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;
import xklusac.extensions.WallclockComparator;

/**
 * Class PBS_PRO<p> This class implements multi-queue priority-based fair share
 * using scheduling policy, similar to the algorithm applied in Czech NGI
 * MetaCentrum.
 *
 * @author Dalibor Klusacek
 */
public class PBS_PRO implements SchedulingPolicy {

    private Scheduler scheduler;

    public PBS_PRO(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        if (!Scheduler.data_set.contains("meta")) {
            double runtime1 = new Date().getTime();
            int index = Scheduler.all_queues_names.indexOf(gi.getQueue());
            if (index == -1) {
                index = 0;
            }

            LinkedList queue = Scheduler.all_queues.get(index);
            queue.addLast(gi);
            Scheduler.runtime += (new Date().getTime() - runtime1);

            return;
        }

        double runtime1 = new Date().getTime();
        //System.out.println("New job has been received by PBS PRO");
        if (Scheduler.data_set.equals("metacentrum.mwf") || Scheduler.data_set.equals("metacentrumE.mwf")) {
            //System.out.println("queue by PBS PRO...");
            if (gi.getQueue().equals("q1")) {
                Scheduler.q1.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q2")) {
                Scheduler.q2.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q3")) {
                Scheduler.q3.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q4")) {
                Scheduler.q4.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q5")) {
                Scheduler.q5.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q6")) {
                Scheduler.q6.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q7")) {
                Scheduler.q7.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q8")) {
                Scheduler.q8.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q9")) {
                Scheduler.q9.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q10")) {
                Scheduler.q10.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("q11")) {
                Scheduler.q11.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            // All remaining non standard queues are considered to be normal
            Scheduler.q3.addLast(gi);
        } else {
            if (gi.getQueue().equals("short")) {
                Scheduler.short_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("normal")) {
                Scheduler.normal_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("quark")) {
                Scheduler.quark_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("long")) {
                Scheduler.long_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("ncbr")) {
                Scheduler.ncbr_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("iti")) {
                Scheduler.iti_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }
            if (gi.getQueue().equals("cpmd")) {
                Scheduler.cpmd_queue.addLast(gi);
                Scheduler.runtime += (new Date().getTime() - runtime1);
                return;
            }

            // All remaining non standard queues are considered to be normal
            Scheduler.normal_queue.addLast(gi);
        }
        Scheduler.runtime += (new Date().getTime() - runtime1);

    }

    /**
     * PBS-pro like algorithm. It is setted up according to the situation used
     * in Czech Grid infrastructure "MetaCentrum".<p> For sharcnet, the setup
     * would be different. Therefore, we do not suggest that you use it as it is
     * but rather take it as a inspiration how multi-queue system with
     * priorities may be implemented.
     */
    @Override
    public int selectJob() {
        //System.out.println("Selecting job by PBS PRO...");
        ResourceInfo r_cand = null;
        int scheduled = 0;

        // we go through all queues according to their priority
        for (int q = 0; q < Scheduler.all_queues.size(); q++) {
                        

            // print priorities
            /*
             * for (int i = 0; i < curr_queue.size(); i++) { GridletInfo gi =
             * (GridletInfo) curr_queue.get(i);
             * System.out.print(Math.round(gi.getPriority())+", ");
             * if(i==curr_queue.size()-1)System.out.println();
            }
             */

            Scheduler.queue = Scheduler.all_queues.get(q);
            Collections.sort(Scheduler.queue, new WallclockComparator());
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
                    /*
                     * if(i>0 && gi.getPriority() > ((GridletInfo)
                     * curr_queue.get(0)).getPriority()){
                     * System.out.println(i+"th job "+gi.getID()+" selected with
                     * bigger fairshare than 0 job, 0 = "+((GridletInfo)
                     * curr_queue.get(0)).getPriority()+"
                     * curr="+gi.getPriority());
                    }
                     */
                    r_cand.addGInfoInExec(gi);
                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(r_cand.resource.getResourceID());
                    scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                    r_cand.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                    // we removed a job from position i so the next job is now on i
                    // we have to decrease the counter otherwise we would skip a job due to i++ in for loop
                    i--;
                    r_cand = null;
                    System.out.println(gi.getID()+" send job by PBS PRO..."+Math.round(GridSim.clock()));
                    return scheduled;

                }

            }//we went through the whole queue

        }// we went through all queues

        return scheduled;

    }
}
