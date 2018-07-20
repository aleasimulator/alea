package xklusac.environment;

import eduni.simjava.Sim_event;
import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import xklusac.extensions.*;
import eduni.simjava.distributions.Sim_normal_obj;
import alea.core.AleaSimTags;

/**
 * Class MWFLoader<p> Loads jobs dynamically over time from the file. Then sends
 * these gridlets to the scheduler. MWF stands for Metacentrum Workloads Format
 * (MWF).
 *
 * @author Dalibor Klusacek
 */
public class MWFLoader extends GridSim {

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
    BufferedReader brc = null;
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
    Random rand;
    double multiplier = 1.0;
    LinkedList<String> cpu_types = new LinkedList();
    LinkedList<Double> cpu_bench = new LinkedList();
    String[] cpus;
    LinkedList<Double> cpu_speed = new LinkedList();
    Input rc = new Input();
    //BufferedReader brc = rc.openFile(new File(folder_prefix + "/data-set/"+data_set+".machines"));
    int maxPErating = 1;
    int skipped = 0;
    int submitted = 0;
    boolean estimates;
    int start_epoch = 0;
    Sim_normal_obj norm;

    /**
     * Creates a new instance of JobLoader
     */
    public MWFLoader(String name, double baudRate, int total_jobs, String data_set, int maxPE, int minPErating, int maxPErating,
            double multiplier, int max_id, boolean estimates) throws Exception {
        super(name, baudRate);
        folder_prefix = System.getProperty("user.dir");
        br = r.openFile(new File(folder_prefix + "/data-set/" + data_set));
        brc = rc.openFile(new File(folder_prefix + "/data-set/" + data_set + ".machines"));
        this.total_jobs = total_jobs;
        this.maxPE = maxPE;
        this.minPErating = minPErating;
        this.data_set = data_set;
        this.multiplier = multiplier;
        this.maxPErating = maxPErating;
        this.submitted = 0;
        this.estimates = estimates;
        // variance = 200 / this will make deviation of at most cca 50% from the mean
        // now - the higher the imprecision should be, the higher the deviation is (maximum is +- 35% (0.7 of scale), minimum is +- 3.3% (0.07 of scale)).
        //this.norm = new Sim_normal_obj("normal distr", ExperimentSetup.userPercentage, ExperimentSetup.userPercentage, (121 + ExperimentSetup.rnd_seed));

        // this will generate normally distributed numbers, mean = 0.0, rest is +-5% from 0.0
        this.norm = new Sim_normal_obj("normal distr", 0.0, 5.0, (121 + ExperimentSetup.rnd_seed));

        this.rand = new Random(12 + ExperimentSetup.rnd_seed);

        //prepare list of CPU properties
        this.cpus = new String[max_id + 1];

        LinkedList<String> machines = new LinkedList();
        rc.getLines(machines, brc);
        for (int m = 0; m < machines.size(); m++) {
            String[] props = machines.get(m).split("\t");
            String description = machines.get(m);
            String[] pe_ids = props[10].split(",");
            for (int c = 0; c < pe_ids.length; c++) {
                int idcpu = Integer.parseInt(pe_ids[c]);
                cpus[idcpu] = description;
            }

        }

        // hardcoded results of our benchmarks
        // in the future, this will be replaced
        cpu_types.add("Pentium3");
        cpu_types.add("AthlonMP");
        cpu_types.add("Xeon");
        cpu_types.add("Xeon");
        cpu_types.add("Opteron");
        cpu_types.add("Itanium2");
        cpu_types.add("Pentium4");
        cpu_types.add("unspecified");
        cpu_bench.add(new Double(1.0));
        cpu_bench.add(new Double(1.771666667));
        cpu_bench.add(new Double(2.88));
        cpu_bench.add(new Double(3.643333333));
        cpu_bench.add(new Double(4.743333333));
        cpu_bench.add(new Double(6.844));
        cpu_bench.add(new Double(1.79));
        cpu_bench.add(new Double(1.0));
        cpu_speed.add(1000.0);
        cpu_speed.add(1800.0);
        cpu_speed.add(2400.0);
        cpu_speed.add(3060.0);
        cpu_speed.add(2200.0);
        cpu_speed.add(1500.0);
        cpu_speed.add(1000.0);
        cpu_speed.add(1000.0);

        if (data_set.equals("metacentrum.mwf") || data_set.equals("metacentrumE.mwf")) {
            start_epoch = 1230768000;
        } else {
            start_epoch = 1199145600;
        }

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
                if (current_gl < total_jobs) {
                    // use delay - next job will be loaded after the simulation time is equal to the previous job arrival.
                    super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                }

                continue;
            }
        }

        super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.SUBMISSION_DONE, new Integer(submitted));
        Sim_event ev = new Sim_event();
        sim_get_next(ev);

        if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            System.out.println("Shuting down the " + data_set + "_JobLoader... with: " + fail + " fails and: " + skipped + " skipped jobs (META)");
        }
        shutdownUserEntity();
        super.terminateIOEntities();


    }

    /**
     * Reads one job from file.
     */
    private ComplexGridlet readGridlet(int j) {

        double speedup = 1.0;

        String[] values = null;
        try {
            values = br.readLine().split("\t");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        int id = Integer.parseInt(values[0]);
        int numCPU;
        try {
            numCPU = Integer.parseInt(values[3]);
        } catch (NumberFormatException ex) {
            numCPU = 1;
        }
        if (numCPU > maxPE) {
            numCPU = maxPE;
        }
        long arrival = 0;
        // 1230768000 is the EPOCH time of 1.1.2009 00:00:00
        arrival = Math.max(0, (Integer.parseInt(values[7]) - start_epoch));
        arrival = Math.round(new Double(arrival) / multiplier);
        //we have to shorten jobs that started before 1.1.2009 00:00:00
        long run_time = 1;
        if (arrival == 0) {
            run_time = (Integer.parseInt(values[8]) + Integer.parseInt(values[10])) - start_epoch;
        } else {
            run_time = Integer.parseInt(values[10]);
        }
        run_time = Math.max(1, run_time);

        //detect speedup TO DO
        String used_cpus[] = values[12].split(" ");
        LinkedList<Double> speedups = new LinkedList();
        for (int k = 0; k < used_cpus.length; k++) {
            int cpu_id = 0;
            if (used_cpus[k].equals("unspecified")) {
                //System.out.println("Chyba: "+values[0]+" , "+values[12]);
                cpu_id = 191; //nympha1.zcu.cz/0
            } else {
                cpu_id = Integer.parseInt(used_cpus[k]);
            }
            String cpu_description = cpus[cpu_id];

            String cpu_desc[] = cpu_description.split("\t");
            String cpu_type = cpu_desc[4];
            Double rel_speed = 1.0 + ((Double.parseDouble(cpu_desc[2]) - cpu_speed.get(cpu_types.indexOf(cpu_type))) / cpu_speed.get(cpu_types.indexOf(cpu_type)));
            speedups.add(rel_speed * cpu_bench.get(cpu_types.indexOf(cpu_type)));
        }
        Double min = new Double(10000000000.0);
        for (int k = 0; k < speedups.size(); k++) {
            Double now = speedups.get(k);
            if (now < min) {
                min = now;
            }
        }
        speedup = Math.round(min * 100000);

        //set the MIPS length        
        double length = Math.round((run_time * speedup) * 1.0);
        length = Math.max(1.0, length);
        if (!ExperimentSetup.use_speeds) {
            length = Math.round(run_time * maxPErating);
        }

        double estimatedMachine = speedup;

        // deadline = (real duration * speedup) + arrival time
        //long duration = Integer.parseInt(values[10]);

        String queue_name = values[2];
        long job_limit = 0;
        if (data_set.equals("metacentrum.mwf")) {
            for (int k = 0; k < 1; k++) {
                if (queue_name.equals("q1")) {
                    job_limit = 720 * 3600;
                    break;
                }
                if (queue_name.equals("q2")) {
                    job_limit = 3600 * 720; // unspecified

                    break;
                }
                if (queue_name.equals("q3")) {
                    job_limit = 3600 * 24;
                    break;
                }
                if (queue_name.equals("q4")) {
                    job_limit = 3600 * 2;
                    break;
                }
                if (queue_name.equals("q5")) {
                    job_limit = 3600 * 24; // unspecified

                    break;
                }
                if (queue_name.equals("q6")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("q7")) {
                    job_limit = 3600 * 720; // unspecified

                    break;
                }
                if (queue_name.equals("q8")) {
                    job_limit = 3600 * 4;
                    break;
                }
                if (queue_name.equals("q9")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("q10")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("q11")) {
                    job_limit = 3600 * 720;
                    break;
                }

                job_limit = 3600 * 24; // normal

            }
        } else {
            for (int k = 0; k < 1; k++) {
                if (queue_name.equals("interactive")) {
                    job_limit = 3600 * 4;
                    break;
                }
                if (queue_name.equals("maintenance")) {
                    job_limit = 3600 * 2; // unspecified

                    break;
                }
                if (queue_name.equals("zsc")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("xentest")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("egee")) {
                    job_limit = 3600 * 24; // unspecified

                    break;
                }
                if (queue_name.equals("pa177")) {
                    job_limit = 3600 * 24;
                    break;
                }
                if (queue_name.equals("parallel")) {
                    job_limit = 3600 * 24; // unspecified

                    break;
                }
                if (queue_name.equals("short")) {
                    job_limit = 3600 * 2;
                    break;
                }
                if (queue_name.equals("normal")) {
                    job_limit = Math.round(3600 * 24 * 1.0);
                    break;
                }
                if (queue_name.equals("quark")) {
                    job_limit = Math.round(3600 * 720 * 1.0);
                    break;
                }
                if (queue_name.equals("long")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("ncbr")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("iti")) {
                    job_limit = 3600 * 720;
                    break;
                }
                if (queue_name.equals("cpmd")) {
                    job_limit = 3600 * 24; // unspecified

                    break;
                }
                job_limit = 3600 * 24; // normal

            }
        }

        // use the estimate as generated by Tsafrir's generator
        if (ExperimentSetup.use_tsafrir) {
            job_limit = Long.parseLong(values[13]);
        }

        

        String properties = values[5];
        String user = values[1];
        properties = properties.replaceAll("\\[", "");
        properties = properties.replaceAll("\\]", "");
        String queue = values[2];

        if (data_set.equals("meta2008.mwf")) {
            if (queue.equals("long")) {
                properties = properties + ";long";
            }
            if (queue.equals("iti")) {
                properties = properties + ";iti";
            }
            if (queue.equals("normal")) {
                properties = properties + ";q_normal";
            }
            if (queue.equals("short")) {
                properties = properties + ";q_short";
            }
            if (queue.equals("pa177")) {
                properties = properties + ";pa177";
            }
            if (queue.equals("privileged")) {
                properties = properties + ";forprivileged";
            }
            if (queue.equals("zsc")) {
                properties = properties + ";zsc";
            }
            if (queue.equals("interactive")) {
                properties = properties + ";q_normal";
            }

        }

        double estimatedLength = 0.0;
        if (estimates) {
            //roughest estimate that can be done = queue limit        
            estimatedLength = Math.round(Math.max((job_limit * maxPErating), length));
            //System.out.println(id+" Estimates "+estimatedLength);
        } else {
            // exact estimates
            estimatedLength = length;
            //System.out.println(id+" Exact "+estimatedLength);
        }

        // setup user's estimates
        //double perc = norm.sample() + ExperimentSetup.userPercentage;

        // obsolete and useless
        double perc = norm.sample();

        int numNodes = 1;
        int ppn = numCPU;
        
        // manually established - fix it according to your needs
        double deadline = job_limit * 2;


        // create one gridlet from one line in data set file
        ComplexGridlet gl = new ComplexGridlet(id, user, job_limit, new Double(length), estimatedLength, 10, 10,
                null, null, arrival, deadline, 1, numCPU, estimatedMachine, queue, properties, perc, 0, numNodes, ppn);
        // and set user id to the Scheduler entity - otherwise it would be returned to the JobLoader when completed.        
        gl.setUserID(super.getEntityId("Alea_3.0_scheduler"));
        return gl;
    }
}
