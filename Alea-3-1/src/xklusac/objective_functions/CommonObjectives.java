/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.objective_functions;


import xklusac.environment.ResourceInfo;
import xklusac.environment.Scheduler;

/**
 * This class contains implementations of common objective functions that can be used when guiding the scheduling algorithm toward desired performance.
 * @author Dalibor
 */
public class CommonObjectives {

    /**
     * Calculates avg. slowdown
     */
    public static double predictAvgSlowdown(double current_time) {
        double sd = 0;
        int jobs = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            sd += ri.accum_sd;
            jobs += ri.resSchedule.size();
        }
        sd = sd / Math.max(1, jobs);
        return Math.max(1.0, sd);
    }

    /**
     * Calculates avg. response time
     */
    public static double predictAvgResponseTime(double current_time) {
        double response = 0;
        int jobs = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            response += ri.accum_resp;
            jobs += ri.resSchedule.size();
        }
        response = response / Math.max(1, jobs);
        return Math.max(0.0, response);
    }

    /**
     * Calculates fairness of current solution (defacto std. deviation from mean normalized user wait time)
     */
    public static double predictFairness(double current_time) {
        double fairness = 0;
        double[] nuwt = new double[Scheduler.users.size()];
        double[] tuwt = new double[Scheduler.users.size()];
        double[] tusa = new double[Scheduler.users.size()];
        double nwt = 0.0;

        // load known values
        for (int i = 0; i < Scheduler.users.size(); i++) {
            nuwt[i] = 0.0;
            tuwt[i] = Scheduler.total_uwt.get(i);
            tusa[i] = Scheduler.users_CPUtime.get(i);
        }
        int u_size = Scheduler.users.size();

        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            // add predicted values
            double[] tuwt2 = ri.updateFairness(tuwt, tusa);
            //double[] tuwt2 = ri.update_tuwt(tuwt);
            //double[] tusa2 = ri.update_tusa(tusa);

            for (int u = 0; u < Scheduler.users.size(); u++) {
                tuwt[u] += tuwt2[u];
                tusa[u] += tuwt2[u + u_size];
            }
        }
        // now tuwt and tusa stores both known and predicted values

        // now proceed with fairness computation
        for (int i = 0; i < Scheduler.users.size(); i++) {
            nuwt[i] = tuwt[i] / Math.max(1.0, tusa[i]);
            nwt += nuwt[i];
        }
        nwt = nwt / (1.0 * Scheduler.users.size());

        // calculate the sum of powers of average normalized wt - normalized user wt
        for (int i = 0; i < Scheduler.users.size(); i++) {
            // to avoid decreasement of values when the power is computed we add 1.0
            fairness += Math.pow((1.0 + (nwt - nuwt[i])), 2.0);
        }
        return Math.max(0.0, fairness);
    }
    
    /**
     * Calculates avg. wait time
     */
    public static double predictAvgWaitTime(double current_time) {
        double wait = 0;
        int jobs = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            wait += ri.accum_wait;
            jobs += ri.resSchedule.size();
        }
        wait = wait / Math.max(1, jobs);
        return Math.max(0.0, wait);
    }

    /**
     * Calculates objective function: Avg. start time
     */
    public static double predictAvgStartTime(double current_time) {
        double start = 0;
        int jobs = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            start += ri.accum_start_time;
            jobs += ri.resSchedule.size();
        }
        start = start / Math.max(1, jobs);
        return Math.max(0.0, start);
    }

    /**
     * Returns expected weighted machine usage wrt. prepared schedule. Use for
     * schedule-based algorithms only!
     */
    public static double predictWeightedMachineUsage(double current_time) {
        double usage = 0.0;
        double makespan = Double.MIN_VALUE;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            usage += ri.res_usage;
            double min = ri.resource_makespan;
            if (min > makespan) {
                makespan = min;
            }
        }
        usage = usage / (Scheduler.availPEs * (makespan - current_time));
        return usage;
    }

    /**
     * Calculate aproximation of how many jobs will meet their deadline. Use for
     * schedule-based algorithms only!
     *
     * @param current_time current simulation time used to predict total
     * tardiness of all jobs in this moment
     */
    public static int predictNumberOfJobsThatMeetDeadline(double current_time) {
        int nondelayed = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            nondelayed += ri.resource_score;
        }
        return nondelayed;
    }

    /**
     * Calculate aproximation of how many jobs will not meet their deadline. Use
     * for schedule-based algorithms only!
     *
     * @param current_time current simulation time used to predict total
     * tardiness of all jobs in this moment
     */
    public static int predictNumberOfDelayedJobs(double current_time) {
        int delayed = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            delayed += ri.expected_fails;
        }
        return delayed;
    }

    /**
     * Calculate aproximation of expected makespan. Use for schedule-based
     * algorithms only!
     *
     * @param current_time current simulation time used to predict total
     * tardiness of all jobs in this moment
     */

    public static double predictMakespan(double current_time) {
        double makespan = Double.MIN_VALUE;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            double min = ri.resource_makespan;
            if (min > makespan) {
                makespan = min;
            }
        }
        return makespan;
    }

    /**
     * Calculates total tardiness
     */
    public static double predictTotalTardiness(double current_time) {
        double total_tardiness = 0.0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            ri.update(current_time);
            total_tardiness += ri.resource_tardiness;
        }
        return total_tardiness;
    }

    /** 
     * Returns actual machine usage. May be used for all algorithms. 
     */
    public static double getActualUsage() {
        int busy = 0;
        double avail = 0;
        for (int i = 0; i < Scheduler.resourceInfoList.size(); i++) {
            ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(i);
            busy += ri.getNumBusyPE();
            avail += ri.getNumRunningPE();
        }
        Scheduler.busyc = busy;
        return Math.round((busy / avail) * 1000) / 10.0;
    }

    /**
     * Calculates objective function: per cluster usage
     */
    public static double getClusterUsage(int c) {
        int busy = 0;
        double avail = 0;

        ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(c);
        busy += ri.getNumBusyPE();

        avail += ri.getNumRunningPE();
        if (avail <= 0) {
            return -100.0;
        }

        if (ri.getNumRunningPE() <= 0) {
            return 0.0;
        } else {
            return Math.round((busy / avail) * 1000) / 10.0;
        }
    }

    /**
     * Calculates percentage of running CPUs
     */
    public static double getClusterStatus(int c) {
        int tot = 0;
        double avail = 0;

        ResourceInfo ri = (ResourceInfo) Scheduler.resourceInfoList.get(c);
        tot = ri.resource.getNumPE();

        avail = ri.getNumRunningPE();

        if (avail <= 0) {
            return 0.0;
        } else {
            return Math.round((avail / tot) * 1000) / 10.0;
        }
    }
}
