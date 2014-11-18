/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.algorithms.schedule_based.optimization;

import gridsim.GridSim;
import java.util.Date;
import java.util.LinkedList;
import xklusac.algorithms.OptimizationAlgorithm;
import xklusac.objective_functions.CommonObjectives;
import xklusac.environment.ExperimentSetup;
import xklusac.environment.GridletInfo;
import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * Class GapSearch<p>
 * This class implements GapSearch optimization algorithm - a backfill like iterative optimization routine.
 * @author       Dalibor Klusacek
 */

public class TabuSearch implements OptimizationAlgorithm {

    public TabuSearch() {
    }

    /**
     * Simple Local Search optimization. Avg. start time, avg. wait time and avg. slowdown are used as decision maker. Jobs are moved to gaps.
     */
    @Override
    public void execute(int rounds, int time_limit) {
        // tabu list
        LinkedList<GridletInfo> tabu = new LinkedList();
        int max_tabu_size = Math.min(10, Scheduler.getScheduleSize()/2);
        
        rounds = rounds * ExperimentSetup.multiplicator;
        time_limit = time_limit * ExperimentSetup.multiplicator;
        double current_time = GridSim.clock();

        Date dd = new Date();
        long start = dd.getTime();

        //empty schedule cannot be optimized
        if (Scheduler.getScheduleSize() > 1) {
            //System.out.println(Math.round(GridSim.clock()) + ": executing Tabu Search on "+Scheduler.getScheduleSize()+" jobs");

            for (int i = 0; i < rounds; i++) {
                double previous_resp = CommonObjectives.predictAvgResponseTime(current_time);
                double previous_fair = CommonObjectives.predictFairness(current_time);
                double previous_wait = CommonObjectives.predictAvgWaitTime(current_time);
                double previous_sd = CommonObjectives.predictAvgSlowdown(current_time);

                int index_prev = findRandonResource();
                ResourceInfo prev_res = (ResourceInfo) Scheduler.resourceInfoList.get(index_prev);
                int gridlet_index = findRandomGridletInfo(index_prev);

                // this schedule has no gridlets
                if (gridlet_index == -1) {
                    continue;
                }
                // remove the gridlet and update the resource internal information
                GridletInfo gi = (GridletInfo) prev_res.removeGInfoIndex(gridlet_index);
                if(tabu.contains(gi)){
                    //System.out.println(gi.getID()+" is in TABU... in round = "+i+" tabu_size/max = "+tabu.size()+"/"+max_tabu_size);
                    prev_res.addGInfo(gridlet_index, gi);
                    prev_res.update(current_time);
                    continue;
                }else{
                    tabu.addLast(gi);
                    if(tabu.size()>max_tabu_size){
                        tabu.removeFirst();
                    }
                }

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
                    boolean found = ri.findHoleForGridlet(gi);
                    if (found) {
                        double new_decision = 0.0;
                        //double current_usage = predictMachineUsage(current_time);
                        //double current_start = predictAvgStartTime(current_time);
                        //double us = Math.max(0.0000000000001, previous_usage);
                        //double diff_u = (current_usage - previous_usage) / us;

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

                        new_decision = (diff_fair * ExperimentSetup.fair_weight) + (diff_rt * 1.0) + (diff_wt * 1.0) + (diff_sd * 1.0);

                        if (new_decision <= 0.0) {
                            /*if (new_decision == 0.0) {
                            succ_eq++;
                            } else {
                            succ_less++;
                            }*/
                            if(diff_fair * ExperimentSetup.fair_weight>0.0){
                              //  System.out.println(ExperimentSetup.fair_weight+ " failure BUT... F = "+Math.round((diff_fair * ExperimentSetup.fair_weight)*100)/100.0+ " SD = "+Math.round(diff_sd*100)/100.0+ " WT = "+Math.round(diff_wt)/100.0+" RT = "+Math.round(diff_rt)/100.0);
                            }
                            ri.removeGInfo(gi);
                            continue;
                        } else {
                            //System.out.println(ExperimentSetup.fair_weight+ " SUCCESS... F = "+Math.round((diff_fair * ExperimentSetup.fair_weight)*100)/100.0+ " SD = "+Math.round(diff_sd*100)/100.0+ " WT = "+Math.round(diff_wt)/100.0+" RT = "+Math.round(diff_rt)/100.0);
                            /*if((diff_fair * ExperimentSetup.fair_weight)<0.0){
                                System.out.println(diff_fair+ " success BUT... F = "+Math.round((diff_fair * ExperimentSetup.fair_weight)*1000)/1000.0+ " SD = "+Math.round(diff_sd*100)/100.0+ " WT = "+Math.round(diff_wt*100)/100.0+" RT = "+Math.round(diff_rt*100)/100.0);
                                System.out.println(current_fair+" -> success BUT... F = "+Math.round((diff_fair * ExperimentSetup.fair_weight)*1000)/1000.0+ " SD = "+Math.round(diff_sd*1000)/1000.0+ " WT = "+Math.round(diff_wt*1000)/1000.0+" RT = "+Math.round(diff_rt*1000)/1000.0+ " SUM = "+((diff_rt * 1.0) + (diff_wt * 1.0) + (diff_sd * 1.0)));
                                for(int u=0; u < ri.resSchedule.size(); u++){
                                    GridletInfo gridl = ri.resSchedule.get(u);
                                    int user_index = Scheduler.users.indexOf(new String(gridl.getUser()));
                                    double tuwt = Scheduler.total_uwt.get(user_index);
                                    double tusa = Scheduler.users_CPUtime.get(user_index);
                                    double nuwt = tuwt / Math.max(1.0, tusa);
                                    System.out.print("("+gridl.getUser()+")"+Math.round(nuwt*10)/10.0+", ");                                    
                                }
                                System.out.println();
                                CommonObjectives.predictFairness2(current_time);
                            }*/
                            succ = true;
                            break;
                        }
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
                    return;
                }
            }
        }
        Scheduler.updateResourceInfos(current_time);
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
