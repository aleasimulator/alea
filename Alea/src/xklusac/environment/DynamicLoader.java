/*
Copyright (c) 2014 Šimon Tóth

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

import eduni.simjava.Sim_event;
import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.util.Random;
import xklusac.extensions.*;
import eduni.simjava.distributions.Sim_normal_obj;
import alea.core.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class DynamicLoader<p>
 * Loads jobs dynamically over time from the file. Then sends these gridlets to
 * the scheduler. SWF stands for Standard Workloads Format (SWF).
 *
 * @author Dalibor Klusacek
 */
public class DynamicLoader extends GridSim {
    /** Dynamic Agents */
    List<GridSim> agents = new ArrayList<GridSim>();

    /**
     * total number of jobs in experiment
     */
    int total_jobs = 0;
    /**
     * start time (for UNIX epoch converting)
     */
    int start_time = -1;
    /**
     * number of PEs in the "biggest" resource
     */
    int maxPE = 1;
    /**
     * minimal PE rating of the slowest resource
     */
    int minPErating = 1;
    int maxPErating = 1;
    /**
     * gridlet counter
     */
    int current_gl = 0;
    /**
     * data set name
     */
    String data_set = "";
    /**
     * counter of failed jobs (as stored in the GWF file)
     */
    int fail = 0;
    int help_j = 0;
    Random rander = new Random(4567);
    double last_delay = 0.0;
    Sim_normal_obj norm;
    double prevl = -1.0;
    double preve = -1.0;
    int prevc = -1;
    long prevram = -1;
    long prev_job_limit = -1;
    int count = 1;

    // total number of agents
    int agents_total = 0;
    // number of agents that finished processing
    int agents_finished = 0;
    
    // number of finished jobs
    int jobs_finished = 0;

    /**
     * Creates a new instance of Dynamic Job Loader
     * @param name Name of this grid sim object.
     * @param baudRate ....
     * @param total_jobs IGNORED
     * @param data_set Name of base configuration file.
     * @param maxPE ...
     * @param minPErating ...
     * @param maxPErating ...
     */
    public DynamicLoader(String name, double baudRate, int total_jobs, String data_set, int maxPE, int minPErating, int maxPErating) throws Exception {
        super(name, baudRate);

        // Read the configuration file
        System.out.println(name + ": reading dynamic simulation configuration file \"" + data_set + "\"");

        Input r = new Input();
        BufferedReader br = r.openFile(new File(get_dataset_filename(data_set)));
        System.out.println(br.toString());

        String line;
        while ((line = br.readLine()) != null) {
            String values[] = line.split("\t");
            String agent_name = values[0];
            // spawn agent
            if (values[1].equalsIgnoreCase("static")) {
                agents.add(new AgentStatic(agent_name,this.getEntityName(),baudRate,total_jobs,data_set,maxPE,minPErating,maxPErating));
                agents_total++;
            }
            else if (values[1].equalsIgnoreCase("dynamic")) {

            }
            else if (values[1].equalsIgnoreCase("dummy_agent")) {

            }
        }

        this.total_jobs = total_jobs;
        this.maxPE = maxPE;
        this.minPErating = minPErating;
        this.maxPErating = maxPErating;
        this.data_set = data_set;
        this.norm = new Sim_normal_obj("normal distr", 0.0, 5.0, (121 + ExperimentSetup.rnd_seed));

    }

    /** Oversee the execution of spawned agents
     *
     *  Once all agents are finished processing, signal end of simulation.
     */
    public void body() {
        super.gridSimHold(10.0);    // hold by 10 second

        // wake up the spawned agents
        for (int i = 0; i < agents.size(); i++) {
            super.sim_schedule(GridSim.getEntityId(agents.get(i).getEntityName()), 0, AleaSimTags.EVENT_WAKE);
        }

        // wait until all agents are finished
        while (agents_finished != agents_total) {
            Sim_event ev = new Sim_event();
            sim_get_next(ev);
            // agent has finished submission
            if (ev.get_tag() == AleaSimTags.AGENT_DONE) {
                Integer total_jobs = (Integer)ev.get_data();
                jobs_finished += total_jobs;
                agents_finished++;
                System.out.println("Agent " + GridSim.getEntityName(ev.get_src()) +  " finished processing after " + total_jobs + " jobs.");
                System.out.println((agents_total - agents_finished) + "/" + agents_total + " running agents remaining.");
            }
        }

        // once all agents are finished, we can wrap up the simulation

        // notify the scheduler about the submission completion
        super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), super.clock(), AleaSimTags.SUBMISSION_DONE, new Integer(jobs_finished));

        // wait for a report back
        Sim_event ev_local = new Sim_event();
        sim_get_next(ev_local);
        if (ev_local.get_tag() == GridSimTags.END_OF_SIMULATION) {
            System.out.println("Shuting down the " + data_set + "_DynamicLoader... with: " + fail + " failed or skipped jobs");
        }

        shutdownUserEntity();
        super.terminateIOEntities();
     }

    private String get_dataset_filename(String data_set) {
        String folder_prefix = System.getProperty("user.dir");

        // Construct filename
        if (ExperimentSetup.data) {
            String[] path = folder_prefix.split("/");
            if (path.length == 1) {
                path = folder_prefix.split("\\\\");
            }
            folder_prefix = "";
            for (int i = 0; i < path.length - 1; i++) {
                folder_prefix += path[i] + "/";
            }
        }

        return folder_prefix + "/data-set/" + data_set;
    }
}
