/*
 Copyright (c) 2015 Simon Toth (kontakt@simontoth.cz)

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
package alea.dynamic;

import xklusac.environment.ComplexGridlet;
import alea.core.WorkloadReaderSWF;
import java.util.HashSet;
import java.util.Set;
import xklusac.environment.ExperimentSetup;

/**
 * Static batch of jobs
 *
 * Static batch is a simple wrapper around the job loader implementation,
 * representing all the jobs of a user.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class JobBatchStatic {

    /**
     * SWF job reader
     */
    private WorkloadReaderSWF job_reader = null;
    private Set<String> read_jobs = null;
    private JobBatchState status = JobBatchState.READY_TO_SUBMIT;

    int jobct = 0;

    /**
     * Initialize the static batch
     */
    public JobBatchStatic(String agent, String data_set, String user, String batch, int maxPE, int minPErating, int maxPErating) throws Exception {
        job_reader = new WorkloadReaderSWF(data_set + "_" + user, maxPE, minPErating, maxPErating, -1);
        job_reader.setDoNotModifyArrivals();
        read_jobs = new HashSet<String>();
    }

    /**
     * Get next job from the batch
     */
    public ComplexGridlet getNextJob() {
        ComplexGridlet job = job_reader.getNextGridlet();
        if (job != null) {
            read_jobs.add(Integer.toString(job.getGridletID()));
            job.setArrival_time(job.getArrival_time() - ExperimentSetup.firstArrival);
            job.setRelease_date(job.getArrival_time());
            jobct++;
            return job;
        } else {
            status = JobBatchState.DONE_SUBMITTING;
            System.out.println("DONE SUBMITTING");
            return null;
        }
    }

    /**
     * Notify this batch about a job completion
     */
    public void notifyJobCompletion(ComplexGridlet job) {
        read_jobs.remove(Integer.toString(job.getGridletID()));
        if (read_jobs.isEmpty() && status == JobBatchState.DONE_SUBMITTING) {
            status = JobBatchState.FINISHED;
            System.out.println("FINISHED");
        }
    }
    
    /**
     * Notify this batch about a job completion
     */
    public void notifyJobFail(ComplexGridlet job) {
        read_jobs.remove(Integer.toString(job.getGridletID()));
        if (read_jobs.isEmpty() && status == JobBatchState.DONE_SUBMITTING) {
            status = JobBatchState.FINISHED;
            System.out.println("FINISHED");
        }
    }

    public JobBatchState getCurrentState() {
        return status;
    }

}
