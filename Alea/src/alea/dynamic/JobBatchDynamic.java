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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import xklusac.environment.ComplexGridlet;
import xklusac.environment.ExperimentSetup;

/**
 * Dynamic Batch implementation
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class JobBatchDynamic {

    private JobBatchState status = JobBatchState.BLOCKED_WAITING;

    private final int sessionID;
    private final int batchID;
    private final int startTime;
    private final int endTime;
    private final List<AbstractMap.SimpleEntry<Integer, Integer>> afterArrival;
    private final List<AbstractMap.SimpleEntry<Integer, Integer>> afterCompletion;
    private final List<String> jobList;
    private final List<String> waitingJobs = new ArrayList<String>();
    private final List<String> runningJobs = new ArrayList<String>();
    private final List<String> completedJobs = new ArrayList<String>();
    private double batchSimulationStart = -1;

    private double firstJobArrival = -1;

    public int getSessionID() {
        return sessionID;
    }

    public int getBatchID() {
        return batchID;
    }

    public JobBatchDynamic(int session_id, int batch_id, int start_time, int end_time,
            List<String> job_list, List<AbstractMap.SimpleEntry<Integer, Integer>> after_arrival, List<AbstractMap.SimpleEntry<Integer, Integer>> after_completion) {
        sessionID = session_id;
        batchID = batch_id;
        startTime = start_time;
        endTime = end_time;
        jobList = job_list;
        afterArrival = after_arrival;
        afterCompletion = after_completion;

        if (afterCompletion.isEmpty() && afterArrival.isEmpty()) {
            status = JobBatchState.READY_TO_SUBMIT;
        }
    }

    public void setRealStart(double time) {
        if (time < 0) {
            batchSimulationStart = startTime - ExperimentSetup.firstArrival;
        }

        batchSimulationStart = time;
    }

    public double getBatchSimulationStart() {
        return batchSimulationStart;
    }

    public void notifyJobEnqueued(String jobid, double time) {
        if (jobList.contains(jobid)) {

            jobList.remove(jobid);
            waitingJobs.add(jobid);

            if (status == JobBatchState.READY_TO_SUBMIT) {
                status = JobBatchState.SUBMITTING;
            }

            if (status == JobBatchState.SUBMITTING && jobList.isEmpty()) {
                status = JobBatchState.DONE_SUBMITTING;
            }

        } else {
            throw new RuntimeException("No more jobs to process in batch, but notifyJobEnqued received.");
        }
    }

    public void notifyJobStarted(String jobid, double time) {
        if (status != JobBatchState.SUBMITTING && status != JobBatchState.DONE_SUBMITTING) {
            throw new RuntimeException("Notify job started received with batch not in state processing.");
        }

        if (waitingJobs.contains(jobid)) {
            waitingJobs.remove(jobid);
            runningJobs.add(jobid);
        } else {
            throw new RuntimeException("Received a start job notification for job that is not registered as waiting.");
        }
    }

    public void notifyJobCompleted(String jobid, double time) {
        if (runningJobs.contains(jobid)) {
            runningJobs.remove(jobid);
            completedJobs.add(jobid);

            if (jobList.isEmpty() && waitingJobs.isEmpty() && runningJobs.isEmpty()) {
                status = JobBatchState.FINISHED;
            }
        }
    }
    
    public void notifyJobFail(String jobid, double time) {
        if (runningJobs.contains(jobid)) {
            runningJobs.remove(jobid);
            completedJobs.add(jobid);
            
            if (jobList.isEmpty() && waitingJobs.isEmpty() && runningJobs.isEmpty()) {
                status = JobBatchState.FINISHED;
            }
            
            return;
        }
        
        if (waitingJobs.contains(jobid)) {
            waitingJobs.remove(jobid);
            completedJobs.add(jobid);
            
            if (jobList.isEmpty() && waitingJobs.isEmpty() && runningJobs.isEmpty()) {
                status = JobBatchState.FINISHED;
            }      
            
            return;
        }
    }
    
    public void dumpJobInfo() {
        for (String j : waitingJobs) {
            System.err.println("\tJob "+j+" still waiting to start.");
        }
        for (String j : runningJobs) {
            System.err.println("\tJob "+j+" still waiting to complete.");
        }
    }
    
    public Boolean hasArrivingJob(String jobid) {
        return waitingJobs.contains(jobid) || runningJobs.contains(jobid);
    } 

    public Boolean hasListJob(String jobid) {
        return jobList.contains(jobid);
    }

    public Boolean hasWaitingJob(String jobid) {
        return waitingJobs.contains(jobid);
    }

    public Boolean hasRunningJob(String jobid) {
        return runningJobs.contains(jobid);
    }

    public Boolean hasCompletedJob(String jobid) {
        return completedJobs.contains(jobid);
    }

    public void notifyBatchArrived(int session_id, int batch_id) {
        //System.out.println("Batch ["+Integer.toString(sessionID)+"-"+Integer.toString(batchID)+"] received batch arrived notification from Batch ["+Integer.toString(session_id)+"-"+Integer.toString(batch_id)+"].");
        AbstractMap.SimpleEntry<Integer, Integer> item = new AbstractMap.SimpleEntry<Integer, Integer>(session_id, batch_id);
        if (afterArrival.contains(item)) {
            afterArrival.remove(item);
        } else {
            return;
        }

        if (afterArrival.isEmpty() && afterCompletion.isEmpty()) {
            if (status != JobBatchState.BLOCKED_WAITING) {
                throw new RuntimeException("Received dependency update for an already unblocked batch.");
            }
            status = JobBatchState.READY_TO_SUBMIT;

            //System.out.println("Batch [" + Integer.toString(sessionID) + "-" + Integer.toString(batchID) + "] batch unblocked after arrival of [" + Integer.toString(session_id) + "-" + Integer.toString(batch_id) + "].");
        }
    }

    public void notifyBatchCompleted(int session_id, int batch_id) {
        //System.out.println("Batch ["+Integer.toString(sessionID)+"-"+Integer.toString(batchID)+"] received batch completed notification from Batch ["+Integer.toString(session_id)+"-"+Integer.toString(batch_id)+"].");
        AbstractMap.SimpleEntry<Integer, Integer> item = new AbstractMap.SimpleEntry<Integer, Integer>(session_id, batch_id);
        if (afterCompletion.contains(item)) {
            afterCompletion.remove(item);
        } else {
            return;
        }

        if (afterArrival.isEmpty() && afterCompletion.isEmpty()) {
            if (status != JobBatchState.BLOCKED_WAITING) {
                throw new RuntimeException("Received dependency update for an already unblocked batch.");
            }
            status = JobBatchState.READY_TO_SUBMIT;
            //System.out.println("Batch [" + Integer.toString(sessionID) + "-" + Integer.toString(batchID) + "] batch unblocked after completion of [" + Integer.toString(session_id) + "-" + Integer.toString(batch_id) + "].");
        }
    }

    public void debugDump() {
        System.out.println("------------------------------------------------------------------");
        System.out.println("Dumping batch information [ SessionID : " + Integer.toString(sessionID) + " ] [ BatchID : " + Integer.toString(batchID) + " ] [ StartTime : " + Integer.toString(startTime) + " ] [ EndTime : " + Integer.toString(endTime) + " ]");
        System.out.println("------------------------------------------------------------------");

        System.out.println("Jobs : ");
        for (String s : jobList) {
            System.out.println("\t" + s);
        }

        System.out.println("After arrival dependencies:");
        for (AbstractMap.SimpleEntry<Integer, Integer> item : afterArrival) {
            System.out.println("\t" + item.getKey().toString() + " : " + item.getValue().toString());
        }

        System.out.println("After completion dependencies:");
        for (AbstractMap.SimpleEntry<Integer, Integer> item : afterCompletion) {
            System.out.println("\t" + item.getKey().toString() + " : " + item.getValue().toString());
        }

        System.out.println("------------------------------------------------------------------");
    }

    public int getStartTime() {
        return startTime;
    }

    public int getFinishTime() {
        return endTime;
    }

    public void setFirstJobArrival(double first_arrival) {
        firstJobArrival = first_arrival;
    }

    public double getFirstArrival() {
        return firstJobArrival;
    }

    /**
     * Get current batch status
     *
     * @return batch status BLOCKED_WAITING, READY_TO_SUBMIT, SUBMITTING, DONE_SUBMITTING or FINISHED
     */
    public JobBatchState getCurrentState() {
        return status;
    }

    public Boolean hasArrived() {
        return (status != JobBatchState.BLOCKED_WAITING);
    }

    public Boolean hasCompleted() {
        return status == JobBatchState.FINISHED;
    }

    public ComplexGridlet adjustTiming(ComplexGridlet job) {
        job.setArrival_time(job.getArrival_time() - firstJobArrival + batchSimulationStart);
        job.setRelease_date(job.getArrival_time());
        return job;
    }

    /**
     * Get next job in this batch.
     *
     * @return Gridlet or null if there are no more jobs in this batch
     */
    public ComplexGridlet getNextJob() {
        return null;
    }
}
