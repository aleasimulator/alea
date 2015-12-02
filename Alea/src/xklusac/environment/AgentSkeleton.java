package xklusac.environment;

import alea.core.AleaSimTags;
import alea.dynamic.JobBatchState;
import eduni.simjava.Sim_event;
import gridsim.GridSim;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Skeleton Helper Class for Dynamic Agents
 *
 * This is a helper class for the implementation of dynamic agents.
 * To create a new agent, create a new subclass and implement received* methods.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public abstract class AgentSkeleton extends GridSim {
    String p_loader;

    /** Construct a new Agent entity in the GridSim environment
     * @param name Name of the agent, all agents need to be uniquely identifiable
     * @param loader_name Name of the dynamic loader entity, needed for completion notifications
     * @param baudRate baud rate from GridSim
     */
    public AgentSkeleton(String name, String loader_name, double baudRate) throws Exception {
        super(name, baudRate);

        p_loader = loader_name;
    }

    /** Agent logic implementation
     *
     * This code runs in a separate thread.
     */
    public final void body() {
        // sleep for 10 seconds of simulation time to avoid initialization errors
        super.gridSimHold(10.0);

        int sent_jobs = 0;

        boolean finishing = false;
        boolean done = false;
        SortedSet<Double> wakeup_events = new TreeSet<Double>();
        wakeup_events.add((double) 0); // initial wakeup
        boolean need_reset = false;

        ComplexGridlet waiting_job = null;

        while (true) {
            Sim_event ev = new Sim_event();
            sim_get_next(ev);

            double clock_now = GridSim.clock();

            boolean should_reset;
            switch (ev.get_tag()) {
                case AleaSimTags.AGENT_ONJOBCOMPL:
                    should_reset = this.onJobCompleted((ComplexGridlet) ev.get_data());
                    need_reset = need_reset || should_reset;
                    break;
                case AleaSimTags.AGENT_ONJOBFAIL:
                    should_reset = this.onJobAborted((ComplexGridlet) ev.get_data());
                    need_reset = need_reset || should_reset;
                    break;
                case AleaSimTags.AGENT_ONJOBSTART:
                    should_reset = this.onJobStarted((ComplexGridlet) ev.get_data());
                    need_reset = need_reset || should_reset;
                    break;
                case AleaSimTags.EVENT_WAKE:
                    wakeup_events.remove(wakeup_events.first());
                    break;
                default:
                    throw new RuntimeException("Unknown event received in AgentSkeleton for "+this.getEntityName()+".");
            }

            if (this.getAgentState() == JobBatchState.DONE_SUBMITTING || this.getAgentState() == JobBatchState.FINISHED) {
                finishing = true;
            }

            if (!finishing) {
                // if we do not have an enqueued job
                if (need_reset || waiting_job == null) {
                    waiting_job = this.getCurrentJob();
                    need_reset = false;
                }

                // if we still don't have a job, deffer for 5 minutes
                if (waiting_job == null) {
                    // wake up ourselves in 5 minutes
                    if (wakeup_events.size() == 0) {
                        wakeup_events.add(clock_now+60*5);
                        super.sim_schedule(super.getEntityId(this.getEntityName()), 60*5, AleaSimTags.EVENT_WAKE);
                    }

                    continue;
                }

                // the job is more than 60 seconds in future
                if (waiting_job.getArrival_time() > clock_now + 60) {
                    // if we have no more pending wakeup events or the next wakeup event is later than the arrival, we need a new wakeup event
                    if (wakeup_events.size() == 0 || wakeup_events.first() > waiting_job.getArrival_time()) {
                        // setup a future wake up event 10 seconds before submit
                        double delay = Math.max(0.0, waiting_job.getArrival_time() - clock_now - 10);
                        wakeup_events.add(clock_now+delay);
                        super.sim_schedule(super.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                    }
                } else {
                    // it is time to submit this job to the GridSim queue
                    double delay = Math.max(0.0, (waiting_job.getArrival_time() - clock_now));
                    waiting_job.setOnJobCompl(this.getEntityName());
                    waiting_job.setOnJobFail(this.getEntityName());
                    waiting_job.setOnJobStart(this.getEntityName());
                    // job information event for scheduler
                    super.sim_schedule(super.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, waiting_job);
                    sent_jobs++;

                    need_reset = need_reset || this.onJobEnqueued(waiting_job);

                    // wake up ourselves after this job
                    if (wakeup_events.size() == 0 || wakeup_events.first() > waiting_job.getArrival_time()) {
                        wakeup_events.add(clock_now+delay);
                        super.sim_schedule(super.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                    }

                    waiting_job = null;
                }
            }

            if (finishing && !done && this.getAgentState() == JobBatchState.FINISHED) {
                    done = true;
            }

            if (done) {
                super.sim_schedule(GridSim.getEntityId(p_loader), 60, AleaSimTags.AGENT_DONE, sent_jobs);
                System.out.println("Agent \"" + this.getEntityName() + "\" finishing after submiting" + sent_jobs + " jobs at \"" + GridSim.clock() + "\".");
                break; // end logic loop
            }
        }

        super.terminateIOEntities();
    }

    /** Implement this method for on-job-enqueued reaction
     *
     * @return Did the agent state change in such a way that wee need to reset the waiting job?
     */
    abstract boolean onJobEnqueued(ComplexGridlet event);

    /** Implement this method for on-job-started reaction
     *
     * @return Did the agent state change in such a way that wee need to reset the waiting job?
     */
    abstract boolean onJobStarted(ComplexGridlet event);

    /** Implement this method for on-job-aborted reaction
     *
     * Could be a transition from Enqueued->Aborted or Started->Aborted
     *
     * @return Did the agent state change in such a way that wee need to reset the waiting job?
     */
    abstract boolean onJobAborted(ComplexGridlet event);

    /** Implement this method for on-job-completed reaction
     *
     * @return Did the agent state change in such a way that wee need to reset the waiting job?
     */
    abstract boolean onJobCompleted(ComplexGridlet event);

    /** Implement this method to fetch the next job for the agent
     *
     * @return NULL if no job is ready yet, or a prepared job
     */
    abstract ComplexGridlet getCurrentJob();

    /** Implement this method to declare the current state of the agent
     *
     * At minimum the following states must be distinctly supported: DONE_SUBMITTING, FINISHED
     *
     * @return Current state of the agent
     */
    abstract JobBatchState getAgentState();
}
