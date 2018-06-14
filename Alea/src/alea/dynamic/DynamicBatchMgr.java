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

import alea.core.BatchReader;
import alea.core.SessionReader;
import alea.core.WorkloadReaderSWF;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import xklusac.environment.ComplexGridlet;

/**
 * Class DynamicBatchMgr<p>
 *
 * Manages batches and sessions under different session models. Responsible for
 * modifying arrival times of jobs according to the model.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class DynamicBatchMgr {

    List<JobBatchDynamic> batches_waiting;
    List<JobBatchDynamic> batches_running;
    List<JobBatchDynamic> batches_completed;
    List<JobSessionDynamic> sessions;

    JobBatchDynamic current_batch;
    JobSessionDynamic current_session = null;
    JobSessionDynamic previous_session = null;

    String p_parent;
    WorkloadReaderSWF swf_reader;

    DynamicModes dynamic_mode = DynamicModes.DEPENDENCY_NATURAL_STATIC;

    public DynamicBatchMgr(String parent_name, String data_set, int maxPE, int minPErating, int maxPErating) throws Exception {

        p_parent = parent_name;
        batches_waiting = new ArrayList<JobBatchDynamic>();
        batches_running = new ArrayList<JobBatchDynamic>();
        batches_completed = new ArrayList<JobBatchDynamic>();

        // read batches
        BatchReader reader = new BatchReader(data_set + "_" + parent_name + "_batches");
        batches_waiting = reader.read_batches();

        SessionReader sreader = new SessionReader(data_set + "_" + parent_name + "_sessions");
        sessions = sreader.read_sessions();

        // TODO - what about the first session?
        current_batch = getNextBatch(0);

        if (current_batch == null) {
            throw new RuntimeException("No unblocked batch found in Dynamic Batch Manager.");
        }

        // initialize workload reader
        swf_reader = new WorkloadReaderSWF(data_set + "_" + parent_name, maxPE, minPErating, maxPErating, 0);
        swf_reader.setDoNotModifyArrivals();
    }

    private JobBatchDynamic getNextBatch(double simulation_time) {
        for (JobBatchDynamic batch : batches_waiting) {
            //System.out.println(simulation_time+" get new batch "+batch.getBatchID()+" of session "+batch.getSessionID()+" batch state:"+batch.getCurrentState());
            if (batch.getCurrentState() == JobBatchState.READY_TO_SUBMIT) {
                double batch_arrival_time = -1;

                // if we are in asap mode, just release the next batch asap
                if (dynamic_mode == DynamicModes.DEPENDENCY_ASAP) {
                    batch_arrival_time = simulation_time;
                    // if we are in session mode, the first session arrives at static time, following sessions arrive 30 minutes after the last completion
                } else if (dynamic_mode == DynamicModes.DEPENDENCY_NATURAL_STATIC) {
                    // if we have a session, determine whether it should still be active
                    if (current_session == null) {
                        if (sessions.isEmpty()) {
                            throw new RuntimeException("No remaining sessions, but we still have active batches.");
                        }
                        current_session = sessions.get(0);
                        sessions.remove(current_session);

                    } else if (!current_session.isAcccepting(simulation_time)) {
                        if (sessions.isEmpty()) {
                            // for now generate a new fake session next morning, at 9 a clock

                            double remainder = simulation_time;

                            double seconds = remainder - Math.floor(remainder / 60) * 60;
                            remainder -= seconds;
                            remainder /= 60;
                            double minutes = remainder - Math.floor(remainder / 60) * 60;
                            remainder -= minutes;
                            remainder /= 60;
                            double hours = remainder - Math.floor(remainder / 24) * 24;
                            remainder -= hours;
                            remainder /= 24;
                            double days = remainder + 1;
                            JobSessionDynamic synthetic_session = new JobSessionDynamic(current_session.getSessionID() + 1, (int) Math.floor(days * 24 * 60 * 60 + 9 * 60 * 60), (int) Math.floor(days * 24 * 60 * 60 + 15 * 60 * 60), 0);
                            previous_session = current_session;
                            current_session = synthetic_session;

                            //throw new RuntimeException("We have run out of sessions.");
                        } else {
                            previous_session = current_session;
                            current_session = sessions.get(0);
                            sessions.remove(current_session);
                        }
                    }

                    batch_arrival_time = current_session.adjustBatchArrival(simulation_time, dynamic_mode);
                }

                batch.setRealStart(batch_arrival_time);
                batches_waiting.remove(batch);
                return batch;
            }
        }

        return null;
    }

    public ComplexGridlet getNextJob(double simulation_time) {
        // check if we are still in an active session
        // if not
        //   if we are in an active batch, then we simply continue, until batch ends
        //   if we are not in an active batch
        //     - determine whether we should prolong the current sessions,
        //     - or to jump to the next meaningful slot (next morning?)

        // when we are jumping to the next batch, determine whether we can fit
        // it in the current session (if any)
        //
        // process batches
        if (current_batch == null) {
            if (batches_waiting.isEmpty()) {
                return null;
            }

            current_batch = getNextBatch(simulation_time);
            //System.out.println("Fetching new batch: "+current_batch.getBatchID());
            if (current_batch == null) {
                return null;
            }
        }

        JobBatchState current_batch_state = current_batch.getCurrentState();
        //System.out.println("-------------------------------------------");
        //System.out.println("This is batch: "+current_batch.getBatchID()+ " with state = "+current_batch_state+" session id = "+current_batch.getSessionID());
        switch (current_batch_state) {
            case READY_TO_SUBMIT: {
                ComplexGridlet job = swf_reader.getNextGridlet();
                //System.out.println(job.getGridletID()+": new job of user "+job.getUser()+" time = "+simulation_time);

                if (job == null) {
                    throw new RuntimeException("Batch in ready state, but no jobs found.");
                }

                if (!current_batch.hasListJob(Integer.toString(job.getGridletID()))) {
                    throw new RuntimeException(current_batch.getBatchID()+ ": Batch <-> Job mismatch, batch does not contain job with ID: "+job.getGridletID());
                }

                // This is the first job that is arriving from this batch
                current_batch.setFirstJobArrival(job.getArrival_time());
                return current_batch.adjustTiming(job);
            }

            // current batch still submiting jobs
            case SUBMITTING: {
                ComplexGridlet job = swf_reader.getNextGridlet();

                if (job == null) {
                    throw new RuntimeException("Batch in processing state, but no jobs found.");
                }

                if (!current_batch.hasListJob(Integer.toString(job.getGridletID()))) {
                    throw new RuntimeException("Batch <-> Job mismatch, batch does not contain job.");
                }

                if (current_batch.getFirstArrival() < 0) {
                    throw new RuntimeException("First Arrival not yet initialized.");
                }

                return current_batch.adjustTiming(job);
            }

            // current batch no longer submiting jobs
            case DONE_SUBMITTING:
                //System.out.println("Done submitting from this batch: "+current_batch.getBatchID()+" with Session id = "+current_batch.getSessionID());
                batches_running.add(current_batch);
                current_batch = getNextBatch(simulation_time);
                //System.out.println("Opening new batch: "+current_batch.getBatchID()+" and getting new job recursively. Session id = "+current_batch.getSessionID());
                //System.out.println("............................");
                return getNextJob(simulation_time);

            // current batch no longer submiting jobs & all jobs managed to finish
            case FINISHED:
                batches_completed.add(current_batch);
                current_batch = getNextBatch(simulation_time);
                return getNextJob(simulation_time);

            // should not be reached
            default:
                throw new RuntimeException("Inconsistent batch state in Dynamic Batch Manager.");
        }
    }

    public JobBatchState getCurrentState() {
        // there are still blocked batches
        if (!batches_waiting.isEmpty()) {
            return JobBatchState.BLOCKED_WAITING;
        }

        // no blocked batches, but we still have an active batch
        if (current_batch != null) {
            return JobBatchState.SUBMITTING;
        }

        // all batches have submitted their jobs, but these jobs are still running
        if (!batches_running.isEmpty()) {
            return JobBatchState.DONE_SUBMITTING;
        }

        return JobBatchState.FINISHED;
    }

    public void notifyJobSubmit(String jobid, double time) {
        for (JobBatchDynamic deps : batches_waiting) {
            deps.notifyBatchArrived(current_batch.getSessionID(), current_batch.getBatchID());
        }
        current_batch.notifyJobEnqueued(jobid, time);
    }

    public void notifyJobStart(ComplexGridlet job, double time) {
        if (current_batch != null && current_batch.hasWaitingJob(Integer.toString(job.getGridletID()))) {
            current_batch.notifyJobStarted(Integer.toString(job.getGridletID()), time);
            return;
        } else {
            for (JobBatchDynamic batch : batches_running) {
                if (batch.hasWaitingJob(Integer.toString(job.getGridletID()))) {
                    batch.notifyJobStarted(Integer.toString(job.getGridletID()), time);
                    return;
                }
            }
        }

        throw new RuntimeException("Started job not found in any known batch.");
    }

    public void notifyJobCompletion(ComplexGridlet job, double time) {
        boolean found = false;

        if (current_batch != null && current_batch.hasRunningJob(Integer.toString(job.getGridletID()))) {
            current_batch.notifyJobCompleted(Integer.toString(job.getGridletID()), time);
            found = true;
        } else {
            for (JobBatchDynamic batch : batches_running) {
                if (batch.hasRunningJob(Integer.toString(job.getGridletID()))) {
                    found = true;
                    batch.notifyJobCompleted(Integer.toString(job.getGridletID()), time);
                    if (batch.hasCompleted()) {
                        for (JobBatchDynamic deps : batches_waiting) {
                            deps.notifyBatchCompleted(batch.getSessionID(), batch.getBatchID());
                        }
                    }
                    break;
                }
            }
        }

        if (current_batch != null && current_batch.hasCompleted()) {
            batches_completed.add(current_batch);
            for (JobBatchDynamic deps : batches_waiting) {
                            deps.notifyBatchCompleted(current_batch.getSessionID(), current_batch.getBatchID());
                        }
                        current_batch = getNextBatch(time);
        }

        Iterator<JobBatchDynamic> it = batches_running.iterator();

        while (it.hasNext()) {
            JobBatchDynamic batch = it.next();

            if (batch.hasCompleted()) {
                batches_completed.add(batch);
                it.remove();
            }
        }

        if (!found) {
            throw new RuntimeException("Completed job not found in any known batch.");
        }
    }

    public void notifyJobFail(ComplexGridlet job, double time) {
        boolean found = false;

        if (current_batch != null && current_batch.hasArrivingJob(Integer.toString(job.getGridletID()))) {
            current_batch.notifyJobFail(Integer.toString(job.getGridletID()), time);
            found = true;
        } else {
            for (JobBatchDynamic batch : batches_running) {
                if (batch.hasArrivingJob(Integer.toString(job.getGridletID()))) {
                    found = true;
                    batch.notifyJobFail(Integer.toString(job.getGridletID()), time);
                    if (batch.hasCompleted()) {
                        for (JobBatchDynamic deps : batches_waiting) {
                            deps.notifyBatchCompleted(batch.getSessionID(), batch.getBatchID());
                        }
                    }
                    break;
                }
            }
        }

        if (current_batch != null && current_batch.hasCompleted()) {
            batches_completed.add(current_batch);
            for (JobBatchDynamic deps : batches_waiting) {
                            deps.notifyBatchCompleted(current_batch.getSessionID(), current_batch.getBatchID());
                        }
            current_batch = getNextBatch(time);
        }

        Iterator<JobBatchDynamic> it = batches_running.iterator();

        while (it.hasNext()) {
            JobBatchDynamic batch = it.next();

            if (batch.hasCompleted()) {
                batches_completed.add(batch);
                it.remove();
            }
        }

        if (!found) {
            throw new RuntimeException("Completed job not found in any known batch.");
        }
    }

    public void dumpJobInfo() {

        System.err.println("Batches waiting " + Integer.toString(batches_waiting.size()) + " Batches runinng " + Integer.toString(batches_running.size()) + " Batches completed " + Integer.toString(batches_completed.size()));
        if (current_batch != null) {
            System.err.println("No current batch.");
        }

        int enqueued = 0;
        int finished = 0;
        int processing = 0;
        int ready = 0;
        int waiting = 0;
        for (JobBatchDynamic ibatch : batches_waiting) {

            if (ibatch.getCurrentState() == JobBatchState.BLOCKED_WAITING) {
                enqueued++;
            }
            if (ibatch.getCurrentState() == JobBatchState.FINISHED) {
                finished++;
            }
            if (ibatch.getCurrentState() == JobBatchState.SUBMITTING) {
                processing++;
            }
            if (ibatch.getCurrentState() == JobBatchState.READY_TO_SUBMIT) {
                ready++;
            }
            if (ibatch.getCurrentState() == JobBatchState.DONE_SUBMITTING) {
                waiting++;
            }
        }

        System.err.println("Out of waiting batches : " + Integer.toString(enqueued) + " batches enqueued, " + Integer.toString(finished) + " batches finished, " + Integer.toString(processing) + " batches processing, "
                + Integer.toString(ready) + " ready batches, " + Integer.toString(waiting) + " waiting batches.");

        if (current_batch != null) {
            current_batch.dumpJobInfo();
        }

        for (JobBatchDynamic batch : batches_waiting) {
            batch.dumpJobInfo();
        }

        for (JobBatchDynamic batch : batches_running) {
            batch.dumpJobInfo();
        }

    }
}
