/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based.optimization;

import gridsim.GridSim;
import java.util.Date;
import xklusac.algorithms.OptimizationAlgorithm;
import xklusac.objective_functions.CommonObjectives;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class RandomSearch<p>
 * This class implements WeightedRandomSearch optimization schedule-based algorithm.
 * @author       Dalibor Klusacek
 */

public class WeightedRandomSearch implements OptimizationAlgorithm {

    public WeightedRandomSearch() {
    }

    /**
     * Simple Local Search optimization. Avg. start time, avg wait timee and avg. slowdown are used as decision maker. Jobs are moved randomly.
     */
    @Override
    public void execute(int rounds, int time_limit) {
        //System.out.println(GridSim.clock() + ": executing Random Search...");
        rounds = rounds * ExperimentSetup.multiplicator;
        time_limit = time_limit * ExperimentSetup.multiplicator;
        double current_time = GridSim.clock();

        Date dd = new Date();
        long start = dd.getTime();

        //empty schedule cannot be optimized
        if (Scheduler.getScheduleSize() > 1) {

            for (int i = 0; i < rounds; i++) {
                double previous_resp = CommonObjectives.predictAvgResponseTime(current_time);
                double previous_wait = CommonObjectives.predictAvgWaitTime(current_time);
                double previous_sd = CommonObjectives.predictAvgSlowdown(current_time);
                double previous_fair = CommonObjectives.predictFairness(current_time);

                int index_prev = findRandonResource();
                ResourceInfo prev_res = (ResourceInfo) Scheduler.resourceInfoList.get(index_prev);
                int gridlet_index = findRandomGridletInfo(index_prev);

                // this schedule has no gridlets
                if (gridlet_index == -1) {
                    continue;
                }
                // remove the gridlet and update the resource internal information
                GridletInfo gi = (GridletInfo) prev_res.removeGInfoIndex(gridlet_index);
                prev_res.update(current_time);
                boolean succ = false;

                // we will test schedules in random order
                int permutation[] = Scheduler.permute(Scheduler.resourceInfoList.size());
                for (int j = 0; j < permutation.length; j++) {

                    ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(permutation[j]);
                    if (!Scheduler.isSuitable(ri, gi)) {
                        continue;
                    }
                    // find new placement for a job
                    int size = Math.max(1, ri.resSchedule.size());
                    int new_position = Scheduler.rand.nextInt(size);
                    ri.addGInfo(new_position, gi);


                    double new_decision = 0.0;
                    //double current_usage = predictMachineUsage(current_time);
                    //double current_start = predictAvgStartTime(current_time);
                    //double us = Math.max(0.0000000000001, previous_usage);
                    //double diff_u = (current_usage - previous_usage) / us;

                    double current_fair = CommonObjectives.predictFairness(current_time);
                    //if(previous_fair > current_fair) {System.out.println(previous_fair+" inic fairness");System.out.println(current_fair);}
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

                    new_decision = (diff_fair * ExperimentSetup.fair_weight) + (diff_rt * 0.0) + (diff_wt * 0.0) + (diff_sd * 0.0);
                    //new_decision = (diff_fair * ExperimentSetup.fair_weight);

                    if (new_decision <= 0.0) {
                        ri.removeGInfo(gi);
                        continue;
                    } else {
                        succ = true;
                        System.out.print("curr fair "+Math.round(current_fair)+" | ");
                        for(int u=0; u < ri.resSchedule.size(); u++){
                            GridletInfo gri = ri.resSchedule.get(u);
                            System.out.print(gri.getUser()+", ");
                        }
                        System.out.println();
                        break;
                    }

                }
                // no better placement was found - return the gridlet to the previous position
                // and make another round of LocalSearch
                if (!succ) {
                    prev_res.addGInfo(gridlet_index, gi);
                }


                Date d_end = new Date();
                long is_end = d_end.getTime();
                if ((is_end - start) >= time_limit) {
                    Scheduler.updateResourceInfos(current_time);
                    System.out.println("------------");
                    return;
                }
            }
        }
        Scheduler.updateResourceInfos(current_time);
        System.out.println("------------");
    }

    /** Randomly selects resource from list. */
    public static int findRandonResource() {
        int index = Scheduler.rand.nextInt(Scheduler.resourceInfoList.size());
        return index;
    }

    /** Randomly selects gridlet from the list of gridlets on this resource. */
    public static int findRandomGridletInfo(int ResIndex) {
        ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(ResIndex);
        int found = -1;
        if (ri.resSchedule.size() > 0) {
            found = Scheduler.rand.nextInt(ri.resSchedule.size());
        }
        return found;
    }
}
