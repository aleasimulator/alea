package xklusac.environment;

import eduni.simjava.Sim_event;
import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import xklusac.extensions.*;
import alea.core.AleaSimTags;

/**
 * Class PWFLoader<p>
 * Loads jobs dynamically over time from the file. Then sends these gridlets to the scheduler. PWF stands for Pisa Workload Format (PWF).
 * @author Dalibor Klusacek
 */
public class PWFLoader extends GridSim {

    /** input */
    Input r = new Input();
    /** current folder */
    String folder_prefix = "";
    /** buffered reader */
    BufferedReader br = null;
    /** total number of jobs in experiment */
    int total_jobs = 0;
    /** start time (for UNIX epoch converting) */
    int start_time = 0;
    /** number of PEs in the "biggest" resource */
    int maxPE = 1;
    /** minimal PE rating of the slowest resource */
    int minPErating = 1;
    int maxPErating = 1;
    /** gridlet counter */
    int current_gl = 0;
    /** data set name */
    String data_set = "";
    /** counter of failed jobs (as stored in the GWF file) */
    int fail = 0;
    int exp = 0;

    /** Creates a new instance of JobLoader */
    public PWFLoader(String name, double baudRate, int total_jobs, String data_set, int maxPE, int minPErating, int maxPErating, int exp) throws Exception {
        super(name, baudRate);
        folder_prefix = System.getProperty("user.dir");
        if (exp < 10) {
            br = r.openFile(new File(folder_prefix + "/data-set/" + data_set));
        } else {
            br = r.openFile(new File(folder_prefix + "/data-set/" + data_set));
        }
        this.total_jobs = total_jobs;
        this.maxPE = maxPE;
        this.minPErating = minPErating;
        this.maxPErating = maxPErating;
        this.data_set = data_set;
        this.exp = exp;
    }

    /** Reads jobs from data_set file and sends them to the Scheduler entity dynamically over time. */
    public void body() {
        super.gridSimHold(10.0);    // hold by 10 second

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
                // some time is needed to transfer this job to the scheduler, i.e., delay should be delay = delay - transfer_time. Fix this in the future.
                super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);

                delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
                if (current_gl < total_jobs) {
                    // use delay - next job will be loaded after the simulation time is equal to the previous job arrival.
                    super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                }

                continue;
            }
        }

        super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.SUBMISSION_DONE, null);
        Sim_event ev = new Sim_event();
        sim_get_next(ev);

        if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            System.out.println("Shuting down the " + data_set + "_JobLoader... with: " + fail + " fails.");
        }
        shutdownUserEntity();
        super.terminateIOEntities();


    }

    /** Reads one job from file. */
    private ComplexGridlet readGridlet(int j) {
        String[] values = null;
        if (j == 0) {
            try {
                br.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            values = br.readLine().split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // such line is not a job description - it is a typo in the GWF file
        int tic = 1;
        int sync = 20;

        int id = Integer.parseInt(values[0]);
        int numCPU = Integer.parseInt(values[1]);
        int arrival = Integer.parseInt(values[4]) * tic;
        arrival += sync;
        double length = Math.round(Integer.parseInt(values[5]) * Integer.parseInt(values[3])) * tic;
        double estimated = Integer.parseInt(values[5]) * maxPErating;
        double estimatedMachine = Integer.parseInt(values[3]);

        int deadline = Integer.parseInt(values[6]);
        double deadline_d = 0.0;
        if (deadline == 2147483647) {
            deadline_d = Double.MAX_VALUE; // it is the same as 2147483647
            //continue;
            } else {
            deadline_d = deadline * tic;
            deadline_d += sync;
        }
        int io = 0;
        for (int i = 7; i < values.length; i++) {
            if (values[i].equals(">")) {
                io = Integer.parseInt(values[i + 1]);
                break;
            }
        }

        long job_limit = Math.round(Integer.parseInt(values[5])) * tic;
        double perc = 0.0;
        //double perc = norm.sample()+ExperimentSetup.userPercentage;       
        
        int numNodes = 1;
        int ppn = numCPU;
        
        ComplexGridlet gl = new ComplexGridlet(id, "John_Doe", job_limit, new Double(length), new Double(estimated), 10, 10,
                "Linux", "Risc arch.", arrival, deadline_d, 1, numCPU, estimatedMachine, "q3", "", perc,0, numNodes, ppn);

        // and set user id to the Scheduler entity - otherwise it would be returned to the JobLoader when completed.
        gl.setUserID(super.getEntityId("Alea_3.0_scheduler"));

        return gl;
    }
}
