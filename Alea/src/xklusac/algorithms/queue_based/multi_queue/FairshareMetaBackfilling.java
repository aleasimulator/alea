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
import xklusac.extensions.WallclockComparator;

/**
 * Class SJF<p> Implements SJF (Shortest Job First) algorithm.
 *
 * @author Dalibor Klusacek
 */
public class FairshareMetaBackfilling implements SchedulingPolicy {

    private Scheduler scheduler;

    public FairshareMetaBackfilling(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void addNewJob(GridletInfo gi) {
        double runtime1 = new Date().getTime();
        int index = Scheduler.all_queues_names.indexOf(gi.getQueue());
        if(index == -1 || ExperimentSetup.by_queue == false){
            index= 0;
        }
        LinkedList queue = Scheduler.all_queues.get(index);
        queue.addLast(gi);
        Scheduler.runtime += (new Date().getTime() - runtime1);
        //System.out.println(gi.getQueue() + " New job has been received in queue " + index);
    }

    @Override
    public int selectJob() {
        boolean backfill = false;
        //System.out.println("Selecting job by FairShareMetaBackfilling...");
        int scheduled = 0;
        ResourceInfo r_cand = null;

        // delete previous reservations
        for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
            ri.deleteReservations();
        }
        //System.out.println("------- mazu rezervace -------");

        for (int q = 0; q < Scheduler.all_queues.size(); q++) {
            Scheduler.queue = Scheduler.all_queues.get(q);
            if (ExperimentSetup.use_fairshare) {
                Collections.sort(Scheduler.queue, new WallclockComparator());
            }
            //System.out.println(Scheduler.queue.size()+" jobs in queue "+Scheduler.all_queues_names.get(q));
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
                    r_cand.addGInfoInExec(gi);
                    // set the resource ID for this gridletInfo (this is the final scheduling decision)
                    gi.setResourceID(r_cand.resource.getResourceID());
                    //System.out.println(gi.getID()+" start in queue "+gi.getQueue()+", avail:"+ExperimentSetup.queues.get(gi.getQueue()).getAvailCPUs()+" of "+ExperimentSetup.queues.get(gi.getQueue()).getLimit()+" req:"+gi.getNumPE());
                    scheduler.submitJob(gi.getGridlet(), r_cand.resource.getResourceID());
                    r_cand.is_ready = true;
                    //scheduler.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_SENT, gi);
                    scheduled++;
                    //System.out.println(gi.getID()+": submit on "+r_cand.resource.getResourceName());
                    r_cand = null;
                    i--;
                    if (backfill) {
                        ExperimentSetup.backfilled++;
                        //System.out.println(gi.getID() + ": backfilled. Queue size = " + Scheduler.queue.size());
                    }
                    return scheduled;
                } else {
                    //if (ExperimentSetup.use_anti_starvation && (GridSim.clock() - gi.getRelease_date()) > Math.min(gi.getJobLimit()/gi.getNumPE(), 3600*12.0)) {
                    //if (ExperimentSetup.use_anti_starvation && (GridSim.clock() - gi.getRelease_date()) > Math.min(gi.getJobLimit()/4.0, 3600*12.0)) {
                    //  && (GridSim.clock() - gi.getRelease_date()) > (60*30)
                    if (ExperimentSetup.anti_starvation && !gi.getQueue().equals("backfill")) {
                        if (ExperimentSetup.use_queues) {
                            int avail = ExperimentSetup.queues.get(gi.getQueue()).getAvailCPUs();
                            // stradej pouze pokud fronta dovoluje
                            if (avail >= gi.getNumPE()) {
                                // zacni stradani - oznac vsechny uzly vhodne pro ulohu
                                for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
                                    ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);

                                    if (Scheduler.isSuitable(ri, gi)) {
                                        ri.markSuitableNodes(gi);
                                        //System.out.println(gi.getID()+": stradam i fronty "+gi.getNumPE()+" CPU na "+ri.resource.getResourceName());
                                    }
                                }
                            }
                        } else {
                            // zacni stradani - oznac vsechny uzly vhodne pro ulohu
                            for (int j = 0; j < Scheduler.resourceInfoList.size(); j++) {
                                ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(j);
                                if (Scheduler.isSuitable(ri, gi)) {
                                    ri.markSuitableNodes(gi);
                                    //System.out.println(gi.getID()+": stradam "+gi.getNumPE()+" CPU na "+ri.resource.getResourceName());
                                }
                            }
                        }
                    }
                    backfill = true;
                    //jdeme na dalsi ulohu, pokud predchozi neslo pustit
                }
            }
        }

        return scheduled;
    }
}
