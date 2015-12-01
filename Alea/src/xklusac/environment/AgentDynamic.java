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
 * Class AgentDynamic<p>
 * Loads jobs dynamically over time from a static workload trace. Then sends
 * these gridlets to the scheduler. Workload strace is expected in SWF. SWF
 * stands for Standard Workloads Format (SWF).
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class AgentDynamic extends AgentSkeleton {

    private DynamicBatchMgr dynamic_mgr = null;
    private ComplexGridlet next_job = null;

    /**
     * Creates a new instance of JobLoader
     */
    public AgentDynamic(String name, String loader_name, double baudRate, String data_set, int maxPE, int minPErating, int maxPErating) throws Exception {
        super(name, loader_name, baudRate);

        dynamic_mgr = new DynamicBatchMgr(name, data_set, maxPE, minPErating, maxPErating);
    }

    @Override
    boolean onJobEnqueued(ComplexGridlet event) {
        dynamic_mgr.notifyJobSubmit(Integer.toString(event.getGridletID()),GridSim.clock());
        next_job = null;
        return true;
    }

    @Override
    boolean onJobStarted(ComplexGridlet event) {
        dynamic_mgr.notifyJobStart(event,GridSim.clock());
        return false;
    }

    @Override
    boolean onJobAborted(ComplexGridlet event) {
        dynamic_mgr.notifyJobFail(event,GridSim.clock());
        return true;
    }

    @Override
    boolean onJobCompleted(ComplexGridlet event) {
        dynamic_mgr.notifyJobCompletion(event,GridSim.clock());
        return true;
    }

    @Override
    ComplexGridlet getCurrentJob() {
        // Try to get the next job
        if (next_job == null) {
            next_job = dynamic_mgr.getNextJob(GridSim.clock());
        }

        return next_job;
    }

    @Override
    JobBatchState getAgentState() {
        return dynamic_mgr.getCurrentState();
    }
}
