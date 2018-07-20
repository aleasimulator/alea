package xklusac.environment;

import alea.core.AleaSimTags;
import eduni.simjava.Sim_event;
import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import xklusac.extensions.*;

/**
 * Class GWFLoader<p>
 * Loads jobs dynamically over time from the file. Then sends these gridlets to
 * the scheduler. GWF stands for Grid Workloads Format (GWF).
 *
 * @author Dalibor Klusacek
 */
public class GWFLoader extends GridSim {

    /**
     * input
     */
    Input r = new Input();
    /**
     * current folder
     */
    String folder_prefix = "";
    /**
     * buffered reader
     */
    BufferedReader br = null;
    /**
     * total number of jobs in experiment
     */
    int total_jobs = 0;
    /**
     * start time (for UNIX epoch converting)
     */
    int start_time = 0;
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
    int submitted = 0;
    double earliest_end_time = 0.0;

    /**
     * Creates a new instance of GWFLoader
     */
    public GWFLoader(String name, double baudRate, int total_jobs, String data_set, int maxPE, int minPErating, int maxPErating) throws Exception {
        super(name, baudRate);
        folder_prefix = System.getProperty("user.dir");

        br = r.openFile(new File(folder_prefix + "/data-set/" + data_set));
        this.total_jobs = total_jobs;
        this.maxPE = maxPE;
        this.minPErating = minPErating;
        this.data_set = data_set;
        this.maxPErating = maxPErating;
        this.submitted = 0;
        this.fail = 0;
    }

    /**
     * Reads jobs from data_set file and sends them to the Scheduler entity
     * dynamically over time.
     */
    public void body() {
        super.gridSimHold(5.0);    // hold by 10 second

        while (current_gl < total_jobs) {

            Sim_event ev = new Sim_event();
            sim_get_next(ev);

            if (ev.get_tag() == AleaSimTags.EVENT_WAKE) {

                ComplexGridlet gl = readGridlet(current_gl);
                current_gl++;
                if (gl == null && current_gl < total_jobs) {
                    super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, AleaSimTags.EVENT_WAKE);
                    continue;
                } else if (gl == null && current_gl >= total_jobs) {
                    continue;
                }
                // to synchronize job arrival wrt. the data set.
                double delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
                submitted++;
                // some time is needed to transfer this job to the scheduler, i.e., delay should be delay = delay - transfer_time. Fix this in the future.
                super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);

                delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
                earliest_end_time = delay + 1;
                if (current_gl < total_jobs) {
                    // use delay - next job will be loaded after the simulation time is equal to the previous job arrival.
                    super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                }

                continue;
            }
        }

        super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), earliest_end_time, AleaSimTags.SUBMISSION_DONE, new Integer(submitted));
        Sim_event ev = new Sim_event();
        sim_get_next(ev);

        if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            System.out.println("Shuting down the " + data_set + "_JobLoader... with: " + fail + " fails. Submitted: " + submitted + " jobs.");
        }
        //shutdownUserEntity();
        //super.terminateIOEntities();

    }

    /**
     * Reads one job from file.
     */
    private ComplexGridlet readGridlet(int j) {
        String[] values = null;

        if (j == 0) {
            while (true) {
                try {
                    values = br.readLine().split("\t");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (!values[0].contains("#")) {
                    break;
                } else {
                    //System.out.println(values[0]);
                }
            }
        } else {
            try {
                values = br.readLine().split("\t");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        // such line is not a job description - it is a typo in the GWF file
        if (values.length < 21) {
            fail++;
            return null;
        }

        // such job failed or was cancelled and no info about runtime or numCPU is available therefore we skip it
        if (values[3].equals("-1") || values[4].equals("-1")) {
            fail++;
            return null;
        }
        int id = Integer.parseInt(values[0]);
        int numCPU;
        try {
            numCPU = Integer.parseInt(values[4]);
        } catch (NumberFormatException ex) {
            System.out.println(values[0] + ": Number parsing error: " + values[4]);
            //ex.printStackTrace();
            numCPU = 1;
        }

        // we do not allow more PEs for one job than there is on the "biggest" machine.
        // Co-allocation is only supported over one cluster (GridResource) by now.
        if (numCPU > maxPE) {
            numCPU = maxPE;

        }

        long arrival = 0;
        // synchronize GridSim's arrivals with the UNIX epoch format as given in GWF
        if (j == 0) {
            start_time = Integer.parseInt(values[1]);
            arrival = 0;

        } else {
            arrival = ((Integer.parseInt(values[1]) - start_time));

        }

        // minPErating is the default speed of the slowest machine in the data set        
        double length = Math.round((Integer.parseInt(values[3])) * maxPErating);

        // queue name
        String queue = "q3";
        String properties = "";
        if (data_set.equals("das2.gwf")) {
            properties = values[20];
        } else {
            properties = values[29];
        }
        // finally create gridlet
        long job_limit = Math.max(Integer.parseInt(values[8]), Integer.parseInt(values[3]));

        double estimatedLength = 0.0;
        if (ExperimentSetup.estimates) {
            //roughest estimate that can be done = queue limit        
            estimatedLength = Math.round(Math.max((job_limit * maxPErating), length));
            //System.out.println(id+" Estimates "+estimatedLength);
        } else {
            // exact estimates
            estimatedLength = length;
            //System.out.println(id+" Exact "+estimatedLength);
        }
        double perc = 0.0;

        int numNodes = 1;
        int ppn = numCPU;

        // manually established - fix it according to your needs
        double deadline = job_limit * 2;

        ComplexGridlet gl = new ComplexGridlet(id, "J", job_limit, new Double(length), estimatedLength, 10, 10,
                null, null, arrival, deadline, 1, numCPU, 0.0, queue, properties, perc, 0, numNodes, ppn);

        // and set user id to the Scheduler entity - otherwise it would be returned to the GWFLoader when completed.
        gl.setUserID(super.getEntityId("Alea_3.0_scheduler"));

        return gl;

    }
}
