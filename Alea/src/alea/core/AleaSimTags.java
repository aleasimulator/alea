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
package alea.core;

/**
 * Simulation Tags specific to Alea Simulator
 *
 * For the remaining Tags @see GridSimTags.
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class AleaSimTags {

    private static final int TAG_BASE = 65536;

    /**
     * Tag for submission done event
     * <p>
     * Event sent by job loaders, proccessed by Scheduler.
     */
    public static final int SUBMISSION_DONE = TAG_BASE + 1;

    /**
     * Tag for gridlet (job information) sent event
     * <p>
     * Event sent by job loaders, processed by Scheduler, currently unused.
     */
    public static final int GRIDLET_SENT = TAG_BASE + 101;

    /**
     * Tag for gridlet (job information) sent event
     * <p>
     * Event sent by job loaders, processed by Scheduler.
     */
    public static final int GRIDLET_INFO = TAG_BASE + 102;

    /**
     * Tag for gridlet (job) started event
     * <p>
     * Event sent by resource allocation policy, processed by Scheduler.
     */
    public static final int GRIDLET_STARTED = TAG_BASE + 103;

    /**
     * Tag for internal wake event
     * <p>
     * Event internally used by several components as a self-wake event.
     */
    public static final int EVENT_WAKE = TAG_BASE + 901;

    /**
     * Tag for internal optimize wake event
     * <p>
     * Event internally used by scheduler as an optimization self-trigger event.
     */
    public static final int EVENT_OPTIMIZE = TAG_BASE + 902;

    /**
     * Tag for interal scheduler wake event
     * <p>
     * Event interally used by scheduler as a self-wake event.
     */
    public static final int EVENT_SCHEDULE = TAG_BASE + 903;

    /**
     * Tag depicting the start of a machine failure
     */
    public static final int FAILURE_START = TAG_BASE + 501;
    /**
     * end of machine failure = internal event in resource policy
     */
    public static final int FAILURE_FINISHED = TAG_BASE + 502;
    /**
     * end of machine failure = machine restart
     */
    public static final int FAILURE_RESTART = TAG_BASE + 503;
    /**
     * internal event in resource policy = end of machine failure
     */
    public static final int FAILURE_INFO = TAG_BASE + 504;
    /**
     * internal event in resource policy - start of a machine failure
     */
    public static final int FAILURE_MACHINE = TAG_BASE + 505;
    /**
     * internal event in resource policy - restart of previously failed machine
     */
    public static final int FAILURE_RESTART2 = TAG_BASE + 506;
    /**
     * periodic print of queue-related data (sim time, number of jobs, free
     * CPUs...)
     */
    public static final int LOG_SCHEDULER = TAG_BASE + 801;
    /**
     * applz decay algorithm periodically
     */
    public static final int FAIRSHARE_WEIGHT_DECAY = TAG_BASE + 701;
    /**
     * update fairshare usage information periodically
     */
    public static final int FAIRSHARE_UPDATE = TAG_BASE + 702;
    /**
     * optimize schedule with local search on-demand
     */
    public static final int SCHEDULER_OPTIMIZE_ONDEMAND = TAG_BASE + 601;
    /**
     * periodic collection of scheduling data - used for visualization
     */
    public static final int SCHEDULER_COLLECT = TAG_BASE + 602;
    
    /**
     * print info on waiting jobs
     */
    public static final int SCHEDULER_PRINT_FIRST_JOB_IN_QUEUE = TAG_BASE + 603;

    /**
     * Agent has finished submission of jobs
     */
    public static final int AGENT_DONE = TAG_BASE + 201;
    public static final int AGENT_RESC = TAG_BASE + 202;
    public static final int AGENT_PERIOD = TAG_BASE + 203;

    /**
     * On Job Start notification tag.
     */
    public static final int AGENT_ONJOBSTART = TAG_BASE + 204;
    /**
     * On Job Completion notification tag.
     */
    public static final int AGENT_ONJOBCOMPL = TAG_BASE + 205;
    
    public static final int AGENT_ONJOBFAIL = TAG_BASE + 206;
    
    /**
     * log info about jobs completed so far
     */
    public static final int SCHEDULER_PRINT_THROUGHPUT = TAG_BASE + 1981;
    
    /**
     * print info about job schedule
     */
    public static final int SCHEDULER_PRINT_SCHEDULE = TAG_BASE + 1982;
    
    
    
}
