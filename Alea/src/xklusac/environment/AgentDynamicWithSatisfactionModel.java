/*
 Copyright (c) 2014-2015 Simon Toth (kontakt@simontoth.cz)

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package xklusac.environment;

import alea.dynamic.DynamicBatchMgr;
import alea.dynamic.JobBatchState;
import gridsim.GridSim;

/**
 * Class AgentDynamicWithSatisfactionModel<p>
 * Loads jobs dynamically over time from a static workload trace. Then sends
 * these gridlets to the scheduler. Workload strace is expected in SWF. SWF
 * stands for Standard Workloads Format (SWF). Agent may "complain"
 * into a log trace if performance is poor.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 * @author Dalibor Klusacek (klusacek@cesnet.cz)
 */
public class AgentDynamicWithSatisfactionModel extends AgentSkeleton {

    private DynamicBatchMgr dynamic_mgr = null;
    private ComplexGridlet next_job = null;
    private double latest_submit = 0.0;
    private long latest_runtime = 0;
    private double total_requested_CPUtime = 0.0;
    private int latest_gid = -1;
    private int user_id = 0;
    private int think_time = 3600;
    private int latest_CPUs = 0;
    private double acceptable_wait = 0.0;
    private ComplexGridlet previous_job = null;
    private int job_count = 0;

    /**
     * Creates a new instance of AgentDynamicWithSatisfactionModel
     */
    public AgentDynamicWithSatisfactionModel(String name, String loader_name, double baudRate, String data_set, int maxPE, int minPErating, int maxPErating, int user_id) throws Exception {
        super(name, loader_name, baudRate);

        dynamic_mgr = new DynamicBatchMgr(name, data_set, maxPE, minPErating, maxPErating);
        this.user_id = user_id;
    }

    @Override
    boolean onJobEnqueued(ComplexGridlet event) {
        dynamic_mgr.notifyJobSubmit(Integer.toString(event.getGridletID()), GridSim.clock());
        next_job = null;

        if (previous_job != null) {
            double actual_wait = GridSim.clock() - latest_submit;
            if (acceptable_wait >= actual_wait) {
                // do not complain, just update acceptable wait
                acceptable_wait -= actual_wait;

            } else {
                if (job_count > 0) {
                    // do complain and then reset everything
                    ExperimentSetup.result_collector.recordLongUserComplain(latest_gid, user_id, this.getEntityName(), GridSim.clock(), (actual_wait / acceptable_wait), job_count);                    
                }
                acceptable_wait = 0;
            }
            if(job_count < 1){
                // no need to remember the "budget"
                acceptable_wait = 0;
            }
        }
        job_count++;
        latest_submit = GridSim.clock();
        latest_runtime = event.getJobLimit();
        latest_CPUs = event.getNumPE();
        latest_gid = event.getGridletID();
        acceptable_wait += ((Math.log(latest_CPUs) + 1) * calculateAcceptableWaitingFromWalltime(latest_runtime));
        previous_job = event;
        
        return true;
    }

    @Override
    boolean onJobStarted(ComplexGridlet event) {
        dynamic_mgr.notifyJobStart(event, GridSim.clock());
        job_count--;
        double actual_wait = GridSim.clock() - latest_submit;
        if (acceptable_wait >= actual_wait) {
            // do not complain, and do nothing
        } else {
            // do complain and then do nothing
            ExperimentSetup.result_collector.recordLongUserComplain(latest_gid, user_id, this.getEntityName(), GridSim.clock(), (actual_wait / acceptable_wait), job_count);
        }

        return false;
    }

    @Override
    boolean onJobAborted(ComplexGridlet event) {
        dynamic_mgr.notifyJobFail(event, GridSim.clock());
        return true;
    }

    @Override
    boolean onJobCompleted(ComplexGridlet event) {
        dynamic_mgr.notifyJobCompletion(event, GridSim.clock());
        return true;
    }

    @Override
    ComplexGridlet getCurrentJob() {
        // Try to get the next job
        if (next_job == null) {
            //System.out.println(this.user_id+ " get new job");
            next_job = dynamic_mgr.getNextJob(GridSim.clock());
        }

        return next_job;
    }

    @Override
    JobBatchState getAgentState() {
        return dynamic_mgr.getCurrentState();
    }

    private long calculateAcceptableWaitingFromWalltime(long walltime) {

        int days = Integer.valueOf(((Long) (walltime / (3600 * 24))).intValue());
        int hours = Integer.valueOf(((Long) (walltime / (3600))).intValue());

        if (days < 1) {
            if (hours < 3) {
                return walltime;
            } else if (hours < 7) {
                // at least 3 hours
                return Math.max(3 * 3600, walltime / 2);
            } else {
                // at least 3.5 hours
                return Math.max(12600, walltime / 3);
            }
        } else {
            if (days < 7) {
                // at least 8 hours
                return Math.max(8 * 3600, walltime / 4);
            } else {
                // at least 42 hours
                return Math.max(42 * 3600, walltime / 5);
            }
        }
    }
}
