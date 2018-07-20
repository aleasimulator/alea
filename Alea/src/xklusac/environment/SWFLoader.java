/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

/**
 *
 * @author Dalibor
 */
import alea.core.AleaSimTags;
import eduni.simjava.Sim_event;
import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import xklusac.extensions.*;
import eduni.simjava.distributions.Sim_normal_obj;

/**
 * Class SWFLoader<p>
 * Loads jobs dynamically over time from the file. Then sends these gridlets to
 * the scheduler. SWF stands for Standard Workloads Format (SWF).
 *
 * @author Dalibor Klusacek
 */
public class SWFLoader extends GridSim {

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

    /**
     * Creates a new instance of JobLoader
     */
    public SWFLoader(String name, double baudRate, int total_jobs, String data_set, int maxPE, int minPErating, int maxPErating) throws Exception {
        super(name, baudRate);
        System.out.println(name + ": openning all jobs from " + data_set);

        folder_prefix = System.getProperty("user.dir");

        System.out.println("Opening job file at: " + ExperimentSetup.data_sets + "/" + data_set);
        br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set));
        this.total_jobs = total_jobs;
        this.maxPE = maxPE;
        this.minPErating = minPErating;
        this.maxPErating = maxPErating;
        this.data_set = data_set;
        this.norm = new Sim_normal_obj("normal distr", 0.0, 5.0, (121 + ExperimentSetup.rnd_seed));

    }

    /**
     * Reads jobs from data_set file and sends them to the Scheduler entity
     * dynamically over time.
     */
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
                //System.out.println("Sending: "+gl.getGridletID());
                last_delay = delay;
                super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), delay, AleaSimTags.GRIDLET_INFO, gl);

                delay = Math.max(0.0, (gl.getArrival_time() - super.clock()));
                if (current_gl < total_jobs) {
                    // use delay - next job will be loaded after the simulation time is equal to the previous job arrival.
                    super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                }

                continue;
            }
        }
        System.out.println("Shuting down - last gridlet = " + current_gl + " of " + total_jobs);
        super.sim_schedule(this.getEntityId("Alea_3.0_scheduler"), Math.round(last_delay + 2), AleaSimTags.SUBMISSION_DONE, new Integer(current_gl));
        Sim_event ev = new Sim_event();
        sim_get_next(ev);

        if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            System.out.println("Shuting down the " + data_set + "_PWALoader... with: " + fail + " failed or skipped jobs");
        }
        shutdownUserEntity();
        super.terminateIOEntities();

    }

    /**
     * Reads one job from file.
     */
    private ComplexGridlet readGridlet(int j) {
        String[] values = null;
        String line = "";

        //System.out.println("Read job "+j);
        if (j == 0) {
            while (true) {
                try {
                    for (int s = 0; s < ExperimentSetup.skipJob; s++) {
                        line = br.readLine();
                    }
                    values = line.split("\t");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (!values[0].contains(";")) {
                    if (line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    if (line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    if (line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    if (line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    values = line.split("\\s+");
                    break;
                } else {
                    //System.out.println("error --- "+values[0]);
                }
            }
        } else {
            try {
                line = br.readLine();
                //System.out.println(">"+line+"<");
                if (line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
                if (line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
                if (line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
                if (line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
                //System.out.println("error1 = "+line+" at gi = "+j);
                values = line.split("\\s+");

            } catch (IOException ex) {
                System.out.println("error = " + values[0] + " at gi = " + j);
                ex.printStackTrace();
            }
        }
        // such line is not a job description - it is a typo in the SWF file
        if (values.length < 5 || values[1].equals("-1")) {
            fail++;
            System.out.println(j + " returning: null " + values[0]);
            return null;
        }

        // such job failed or was cancelled and no info about runtime or numCPU is available therefore we skip it
        if (values[3].equals("-1") || values[4].equals("-1")) {
            fail++;
            //System.out.println("returning: null2 ");
            return null;
        }
        //System.out.println(values[0]+"+"+values[1]+"+"+values[2] + ": Number parsing error: " + values[4]);
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
        if (start_time < 0) {
            //System.out.println("prvni: "+j+" start at:"+values[1]+" line="+line);
            start_time = Integer.parseInt(values[1]);
            arrival = 0;

        } else {

            arrival = ((Integer.parseInt(values[1]) - start_time));
            //System.out.println("pokracujeme..."+arrival);

        }
        arrival = Math.round(new Double(arrival) / ExperimentSetup.arrival_rate_multiplier);

        // minPErating is the default speed of the slowest machine in the data set        
        double length = Math.round((Integer.parseInt(values[3])) * maxPErating);

        // queue name
        String queue = values[14];

        // requested RAM = KB per node (not CPU)
        long ram = Long.parseLong(values[9]);
        if (ram == -1) {
            if (Long.parseLong(values[9]) == -1) {
                //System.out.println(id + " not specified RAM, setting 1 KB...");
                ram = 1;
            } else {
                ram = Long.parseLong(values[9]);
            }
        } else {
            if (data_set.contains("zewura") || data_set.contains("wagap") || data_set.contains("meta") || data_set.contains("ncbr")) {

                ram = Math.round(ram / 1024.0);
                queue = values[19];
            }
            double gbram = Math.round(ram * 10 / 1048576.0) / 10.0;
            //System.out.println(id+ " requests "+ram+" KB RAM, "+gbram+" GB RAM per "+numCPU+" CPUs");
        }
        if (!ExperimentSetup.use_RAM) {
            ram = 0;
        }

        // skip such job
        /*if (data_set.contains("zewura") || data_set.contains("wagapp") || data_set.contains("meta")) {
         if (!queue.equals("short") && !queue.equals("long") && !queue.equals("normal") && !queue.equals("backfill") && !queue.equals("preemptible")) {
         fail++;
         return null;
         }
         }
         */
        //SKIP
        /*
         * if (id < 172262) { fail++; return null; }
         */
        // finally create gridlet
        //numCPU = 1;
        long job_limit = Integer.parseInt(values[8]);
        if (job_limit < 0) {
            // atlas = 432000
            // thunder = 432000
            if (data_set.equals("thunder.swf")) {
                job_limit = 48000; //13 hours 20 min
                ExperimentSetup.max_estim++;
            } else if (data_set.equals("atlas.swf")) {
                job_limit = 73200; //20 hours 20 minutes
                ExperimentSetup.max_estim++;
            } else if (data_set.equals("star.swf")) {
                job_limit = 64800; //18 hours
                ExperimentSetup.max_estim++;
            } else if (data_set.equals("ctc-sp2.swf")) {
                job_limit = 64800; //18 hours
                ExperimentSetup.max_estim++;
            } else if (data_set.equals("blue.swf")) {
                job_limit = 36 * 3600; //2 hours
                ExperimentSetup.max_estim++;
            } else if (data_set.equals("kth-sp2.swf")) {
                job_limit = 14400; //4 hours
                ExperimentSetup.max_estim++;
            } else if (data_set.equals("sandia.swf")) {
                job_limit = 18000; //5 hours
                ExperimentSetup.max_estim++;
            } else {
                job_limit = Integer.parseInt(values[3]);
            }
        }

        double estimatedLength = 0.0;
        if (ExperimentSetup.estimates) {
            //roughest estimate that can be done = queue limit        
            estimatedLength = Math.round(Math.max((job_limit * maxPErating), length));
            //System.out.println(id+" Estimates "+estimatedLength+" real = "+length);
        } else {
            // exact estimates
            estimatedLength = length;
            //System.out.println(id+" Exact "+estimatedLength);
        }

        String user = values[11];

        //System.out.println(id + " requests " + ram + " KB RAM per " + numCPU + " CPUs, user: " + user + ", length: " + length + " estimatedLength: " + estimatedLength);
        int numNodes = -1;
        int ppn = -1;
        String properties = "";
        if (values.length > 19) {
            properties = values[20];

            if (data_set.contains("wagap") || data_set.contains("meta") || data_set.contains("ncbr") || data_set.contains("fairshare")) {
                String[] req_nodes = values[20].split(":");
                properties = values[20];
                for (int r = 0; r < req_nodes.length; r++) {
                    if (req_nodes[r].contains("ppn=")) {
                        String ppns = req_nodes[r].replace("ppn=", "");
                        if (ppns.contains("#")) {
                            int ind = ppns.indexOf("#");
                            ppns = ppns.substring(0, ind);
                        }
                        // remove floating point values
                        if (ppns.contains(".")) {
                            int ind = ppns.indexOf('.');
                            ppns = ppns.substring(0, ind);
                        }

                        // to do: 1:ppn=1+3:ppn=2 
                        if (ppns.contains("+")) {
                            break;
                        }
                        if (ppns.equals("1cl_zewura")) {
                            ppn = 1;
                        } else {
                            ppn = Integer.parseInt(ppns);
                        }
                    }
                }

                if (ppn != -1) {
                    // korekce chyby ve workloadu
                    if (numCPU < ppn) {
                        System.out.println(id + ": CPUs mismatch CPUs = " + numCPU + " nodespec = " + properties);
                        numCPU = ppn;
                    }
                    numNodes = numCPU / ppn;
                } else {
                    numNodes = 1;
                    ppn = numCPU;
                }
                //System.out.println(id+" | "+values[20]+" nodes="+numNodes+" ppn="+ppn);
            } else if (data_set.contains("zewura")) {
                numNodes = 1;
                ppn = numCPU;
            } else if (data_set.contains("hpc2n")) {
                ppn = 2;
                if (numCPU < ppn) {
                    ppn = numCPU;
                    numNodes = 1;
                } else if (numCPU % 2 == 1) {
                    ppn = 1;
                    numNodes = numCPU;
                } else {
                    Long nn = Math.round(Math.ceil(numCPU / ppn));
                    numNodes = nn.intValue();
                }
                if (ppn * numNodes != numCPU) {
                    System.out.println(id + ": numNodes value is wrong, CPUs = " + numCPU + " ppn = " + ppn);
                }
            }

            if (ppn * numNodes != numCPU) {
                System.out.println(id + ": CPUs mismatch CPUs = " + numCPU + " ppn = " + ppn + " nodes = " + numNodes);
                numCPU = ppn * numNodes;
            }
        }

        // obsolete and useless
        double perc = norm.sample();

        job_limit = Math.max(1, Math.round(job_limit / ExperimentSetup.runtime_minimizer));
        length = Math.max(1, Math.round(length / ExperimentSetup.runtime_minimizer));
        estimatedLength = Math.max(1, Math.round(estimatedLength / ExperimentSetup.runtime_minimizer));

        if (data_set.contains("wagap")) {
            if (ppn > 8) {
                properties += ":^cl_zigur";
            }
            if (ppn > 12) {
                properties += ":^cl_zegox";
            }
            if (ppn > 16) {
                properties += ":^cl_zapat";
            }
        }

        if (queue.equals("backfill") && data_set.contains("meta")) {
            properties += ":^cl_manwe:^cl_mandos:^cl_skirit:^cl_ramdal:^cl_haldir:^cl_gram";
        }

        if (queue.equals("mikroskop") || queue.equals("quark")) {
            properties += ":cl_quark";
        }

        if (queue.contains("ncbr")) {
            properties += ":cl_perian";
        }

        if (!Scheduler.all_queues_names.contains(queue) && ExperimentSetup.use_queues) {
            fail++;
            System.out.println("Unknown queue " + queue + " - skipping job " + id);
            return null;
        }

        // manually established - fix it according to your needs
        double deadline = job_limit * 2;
        ComplexGridlet gl = new ComplexGridlet(id, user, job_limit, new Double(length), estimatedLength, 10, 10,
                "Linux", "Risc arch.", arrival, deadline, 1, numCPU, 0.0, queue, properties, perc, ram, numNodes, ppn);

        // and set user id to the Scheduler entity - otherwise it would be returned to the JobLoader when completed.
        //System.out.println(id+" job has limit = "+(job_limit/3600.0)+" queue = "+queue);
        gl.setUserID(super.getEntityId("Alea_3.0_scheduler"));
        return gl;
    }
}
