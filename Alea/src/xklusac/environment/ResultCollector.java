/*
 * ResultCollector.java
 *
 * Created on 4. listopad 2009, 12:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package xklusac.environment;

import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import xklusac.extensions.Input;
import xklusac.extensions.Output;
import xklusac.plugins.Plugin;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class ResultCollector<p>
 * This class stores results into csv file(s) and graphs (future work).
 *
 * @author Dalibor Klusacek
 */
public class ResultCollector {

    int tot = 0;
    public LinkedList results;
    public String data_set;
    Output out = new Output();
    String problem = "";
    String output_name = "";
    PrintWriter pw = null;
    PrintWriter pw2 = null;
    PrintWriter pwc = null;
    double TSA = 0.0;
    double SAJ = 0.0;
    double SDJ = 0.0;
    double SSDJ = 0.0;
    int succ_m = 0;
    int bad = 0;
    double job_time = 0.0;
    double avail_time = 0.0;
    //static double failure_time = 0.0;
    double wjob_time = 0.0;
    double wavail_time = 0.0;
    //static double wfailure_time = 0.0;
    double av_PEs = 0.0;
    double wav_PEs = 0.0;
    double day_usage = 0.0;
    double week_usage = 0.0;
    int week_count = 0;
    //double run_time = 0.0;
    /**
     * denotes total flow time
     */
    double flow_time = 0.0;
    /**
     * denotes total wait time
     */
    double wait_time_global = 0.0;
    /**
     * auxiliary variable
     */
    private double sa_total = 0.0;
    /**
     * auxiliary variable
     */
    private double slowdown = 0.0;
    /**
     * auxiliary variable
     */
    private double awrt = 0.0;
    /**
     * auxiliary variable
     */
    private double awsd = 0.0;
    /**
     * auxiliary variable
     */
    private int failed;
    /**
     * auxiliary variable
     */
    private int success;
    private int backfilled;
    double succ_flow = 0.0;
    double succ_wait = 0.0;
    double succ_slow = 0.0;
    double bound_succ_slow = 0.0;
    /**
     * Total tardiness of this schedule
     */
    private double tardiness = 0.0;
    /**
     * deadline score
     */
    private int real_score = 0;
    /**
     * deadline score
     */
    private int neg_score = 0;
    /**
     * auxiliary variable
     */
    private int received = 0;
    private String user_dir = "";

    private List<Plugin> plugins = new ArrayList<Plugin>();

    /**
     * Creates a new instance of ResultCollector
     */
    public ResultCollector(LinkedList results, String prob) {
        user_dir = System.getProperty("user.dir");

        /*if (ExperimentSetup.meta) {
         user_dir = "/scratch/klusacek/" + ExperimentSetup.path;
         } else {
         user_dir = System.getProperty("user.dir");
         }*/
        this.results = results;
        this.problem = prob;
        /*try {
         //System.out.println("!&&&&&&&&&&&&&&&&&& "+user_dir + "/Results("+problem+").csv");
         out.deleteResults(user_dir + "/Results(" + problem + ").csv");
         out.deleteResults(user_dir + "/WGraphs(" + problem + ").csv");
         out.deleteResults(user_dir + "/RGraphs(" + problem + ").csv");
         out.deleteResults(user_dir + "/SGraphs(" + problem + ").csv");
         } catch (IOException ex) {
         ex.printStackTrace();
         }*/
    }

    public int getReceived() {
        return received;
    }

    /**
     * generate results headers
     */
    public void generateHeader(String data_set, String[] pluginHeaders) {
        String waxis = "algorithm";
        int whours = 0;
        for (int i = 0; i < 1442; i++) {
            if (i % 60 == 0) {
                waxis += "\t" + whours;
                whours++;
            } else {
                waxis += "\t ";
            }
        }

        String raxis = "algorithm";
        int rhours = 0;
        for (int i = 0; i < 2882; i++) {
            if (i % 60 == 0) {
                raxis += "\t" + rhours;
                rhours++;
            } else {
                raxis += "\t-";
            }
        }

        String saxis = "algorithm";
        for (int i = 0; i < 1001; i++) {
            saxis += "\t" + i;
        }
        saxis += "\t>1000";

        // String with all plugin headers
        String headersOfPlugins = generatePluginHeaders(pluginHeaders);

        try {
            out.writeString(user_dir + "/Results(" + problem + ").csv", "1/" + data_set
                    + "\tsubmit.\tcompl.\tkilled\tresp_time\truntime\tsch-cr-time\tmakespan\tweigh_usg\tclass_usg\ttardiness\twait\tsld\tawrt\tawsd\ts_resp\ts_wait\ts_sld\tbounded_sld\tbackfilled" + headersOfPlugins);
            out.writeString(user_dir + "/WGraphs(" + problem + ").csv", waxis);
            out.writeString(user_dir + "/SGraphs(" + problem + ").csv", saxis);
            out.writeString(user_dir + "/RGraphs(" + problem + ").csv", raxis);

            //out.writeString(user_dir + "/FairGraf.csv", "time\tuser\tCPUt\tRAM\tmult\tjobs\tfairshare\tuser\tCPUt\tRAM\tmult\tjobs\tfairshare\tuser\tCPUt\tRAM\tmult\tjobs\tfairshare\tuser\tCPUt\tRAM\tmult\tjobs\tfairshare\tuser\tCPUt\tRAM\tmult\tjobs");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Serializes headers into a String.
     *
     * @param headers array with all headers
     *
     * @return headers in one String
     */
    private static String generatePluginHeaders(String[] headers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            sb.append("\t");
            sb.append(headers[i]);
        }
        String headerResult = sb.toString();
        return headerResult;
    }

    /**
     * generate results - stores into csv file
     */
    public void generateResults(String suff, int experiment_count) {
        // calculate and print output results for this setup of experiment
        double completed_jobs = 0.0;
        double avg_time = 0.0;
        double avg_makespan = 0.0;
        double wait_time = 0.0;
        double classic_usage = 0.0;
        int neg_score = 0;
        double flow_time = 0.0;
        double machine_usage = 0.0;
        double creation_time = 0.0;
        double tardiness = 0.0;
        double slowdown = 0.0;
        double awrt = 0.0;
        double awsd = 0.0;
        int submitted = 0;
        double succ_flow = 0.0;
        double succ_wait = 0.0;
        double succ_slow = 0.0;
        double b_succ_slow = 0.0;
        int backfilled = 0;
        double[] pluginsValues = new double[plugins.size()];

        for (int i = 0; i < results.size(); i = i + 19 + plugins.size()) {

            // deadline score
            completed_jobs += (Integer) results.get(i);
            // scheduling time
            avg_time += (Double) results.get(i + 1);
            // makespan
            avg_makespan += (Double) results.get(i + 2);
            // classic machine usage
            classic_usage += (Double) results.get(i + 3);
            // scalability results
            wait_time += (Double) results.get(i + 4);
            // negative score
            neg_score += (Integer) results.get(i + 5);
            // flow time
            flow_time += (Double) results.get(i + 6);
            // weighted machine usage
            machine_usage += (Double) results.get(i + 7);
            // time to create schedule
            creation_time += (Double) results.get(i + 8);
            // tardiness
            tardiness += (Double) results.get(i + 9);
            // slowdown
            slowdown += (Double) results.get(i + 10);
            // avg. weigh. response time
            awrt += (Double) results.get(i + 11);
            // avg. weigh. slowdown
            awsd += (Double) results.get(i + 12);

            submitted += (Integer) results.get(i + 13);
            succ_flow += (Double) results.get(i + 14);
            succ_wait += (Double) results.get(i + 15);
            succ_slow += (Double) results.get(i + 16);
            b_succ_slow += (Double) results.get(i + 17);
            backfilled += (Integer) results.get(i + 18);

            for (int j = 0; j < plugins.size(); j++) {
                pluginsValues[j] += (Double) results.get(i + j + 19);
            }
        }
        // print results (deadline score and scheduling time and makespan)
        //System.out.println("DataSet: " + data_set);
        System.out.println("-----------------------------------------------------------------------------------------------------------");
        System.out.println(" ResultCollector - generates results for " + (Math.round(submitted * 100.0) / (experiment_count * 100.0)) + " submitted jobs.");
        //String fair = "user\ttuwt[i]\tnuwt[i]\ttusa[i]\tnwt";
        String fair = "";

        if (ExperimentSetup.algID == ExperimentSetup.prevAlgID) {
            fair += "-------------------------------\n";
        }
        fair = fair + "" + calculateUserStatistics();

        System.out.println("-----------------------------------------------------------------------------------------------------------");
        String prob = "_";
        prob += ExperimentSetup.algID + "_" + ExperimentSetup.name;

        if (ExperimentSetup.estimates) {
            prob += "_estim";
        }
        if (!ExperimentSetup.estimates) {
            prob += "_exact";
        }

        String pluginResultString = getPluginResultString(pluginsValues, experiment_count);

        try {
            // delete old one, will be left at the end
            if (ExperimentSetup.algID != ExperimentSetup.prevAlgID) {
                out.deleteResults(user_dir + "/Users" + prob + ".csv");
            }

            out.writeString(user_dir + "/Users" + prob + ".csv", fair);
            out.writeString(user_dir + "/Results(" + problem + ").csv", suff + "\t"
                    + Math.round(submitted * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(completed_jobs * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(neg_score * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(flow_time * 100) / (experiment_count * 100.0) + "\t"
                    + Math.round(avg_time * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(creation_time * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(avg_makespan) / (experiment_count) + "\t"
                    + Math.round(machine_usage * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(classic_usage * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(tardiness * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(wait_time * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(slowdown * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(awrt * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(awsd * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(succ_flow * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(succ_wait * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(succ_slow * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(b_succ_slow * 100.0) / (experiment_count * 100.0) + "\t"
                    + Math.round(backfilled * 100.0) / (experiment_count * 100.0)
                    + pluginResultString);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        generateCDFJobsStatistics(suff, completed_jobs);

        results.clear();
    }

    /**
     * Serialize all plugin values in one String.
     *
     * @param pluginsValues an array with results from all plugins
     * @param experiment_count the number of experiments
     *
     * @return one string with all values
     */
    private String getPluginResultString(double[] pluginsValues, int experiment_count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pluginsValues.length; i++) {
            sb.append("\t");
            sb.append(Math.round(pluginsValues[i] * 100.0) / (experiment_count * 100.0));
        }
        String pluginResultString = sb.toString();
        return pluginResultString;
    }

    /**
     * Deletes results.
     */
    public void deleteSchedResults(String suff) {
        try {
            // delete files with old simulation results
            //out.deleteResults(user_dir + "/actual_usage_" + suff + ".csv");
            //out.deleteResults(user_dir + "/waiting_" + suff + ".csv");
            //out.deleteResults(user_dir + "/running_" + suff + ".csv");
            //out.deleteResults(user_dir + "/day_usage_" + suff + ".csv");
            //out.deleteResults(user_dir + "/week_usage_" + suff + ".csv");
            String prob = "_";
            prob += ExperimentSetup.algID + "_" + ExperimentSetup.name;

            if (ExperimentSetup.estimates) {
                prob += "_estim";
            }
            if (!ExperimentSetup.estimates) {
                prob += "_exact";
            }
            if (!ExperimentSetup.anti_starvation) {
                prob += "_bez";
            } else {
                prob += "_stradani";
            }
            this.output_name = user_dir + "/jobs" + prob + ".csv";
            out.deleteResults(output_name);

            this.pw = new PrintWriter(new FileWriter(FileUtil.getPath(output_name)), true);

            //this.pw = new PrintWriter(new FileWriter(output_name, true));
            this.pw2 = new PrintWriter(new FileWriter(FileUtil.getPath(user_dir + "/jobs(" + problem + "" + ExperimentSetup.algID + ").csv"), true));
            this.pwc = new PrintWriter(new FileWriter(FileUtil.getPath(user_dir + "/complain(" + problem + "" + ExperimentSetup.algID + ").csv"), true));

            out.writeStringWriter(pw, "giID \t arrival \t wait \t runtime \t CPUs \t RAM \t userID \t queue");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Stores all information about currently finished job.
     */
    public void addFinishedJobToResults(ComplexGridlet gridlet_received, ArrayList resourceInfoList) {
        GridletInfo gi = new GridletInfo(gridlet_received);
        received++;
        double finish_time = 0.0;
        double cpu_time = 0.0;
        double mips = 0.0;
        double arrival = 0.0;
        if (gridlet_received.getGridletStatus() == Gridlet.FAILED_RESOURCE_UNAVAILABLE || gridlet_received.getGridletStatus() == Gridlet.FAILED) {
            failed++;
            finish_time = Math.max(gi.getGridlet().getArrival_time(), (gi.getGridlet().getExecStartTime() + gi.getGridlet().getActualCPUTime()));
            cpu_time = Math.max(1.0, gi.getGridlet().getActualCPUTime());
            mips = gridlet_received.getGridletFinishedSoFar();
            arrival = gi.getGridlet().getArrival_time();
            System.out.println(gi.getID() + " returned failed, time = " + GridSim.clock());

        } else if (gridlet_received.getGridletStatus() == Gridlet.CANCELED) {
            failed++;
            finish_time = GridSim.clock();
            cpu_time = 0.0;
            arrival = gi.getGridlet().getArrival_time();
            mips = 0.0;
            System.out.println(gi.getID() + " returned canceled, time = " + GridSim.clock());

        } else {
            success++;
            finish_time = gi.getGridlet().getFinishTime();
            cpu_time = gi.getGridlet().getActualCPUTime();
            arrival = gi.getGridlet().getArrival_time();
            mips = gridlet_received.getGridletLength();
            double succ_resp = Math.max(0.0, (finish_time - arrival));
            succ_flow += succ_resp;
            succ_wait += Math.max(0.0, (succ_resp - cpu_time));
            tot++;

            succ_slow += Math.max(1.0, (succ_resp / Math.max(1.0, cpu_time)));
            // bacha zmena 1.0 -> 10.0
            bound_succ_slow += Math.max(1.0, (succ_resp / Math.max(ExperimentSetup.sld_tresh, cpu_time)));
        }
        //run_time += cpu_time;

        gi.setTardiness(Math.max(0, finish_time - gridlet_received.getDue_date()));
        //System.out.println(gi.getID()+" deadline = "+gi.getDue_date()+" tardiness = "+gi.getTardiness());

        // calculate various results
        double g_tard = gi.getTardiness();
        tardiness += g_tard;
        if (g_tard <= 0.0) {
            real_score++;
        } else {
            neg_score++;
        }

        double response = Math.max(0.0, (finish_time - arrival));
        double saj = gi.getNumPE() * mips;

        // utilized time by job
        job_time += gi.getNumPE() * cpu_time;
        wjob_time += gi.getNumPE() * gridlet_received.getGridletFinishedSoFar();
        flow_time += response;

        //interates all plugins and cumulates their value
        for (Plugin pl : plugins) {
            pl.cumulate(gridlet_received);
        }

        wait_time_global += Math.max(0.0, (response - cpu_time));
        // slowdown must be >= than 1.0, response may be 0 so normalize
        slowdown += Math.max(1.0, (response / Math.max(1.0, cpu_time))); // prevent division by zero

        sa_total += saj;
        awrt += saj * response;
        awsd += saj * Math.max(1.0, ((response / Math.max(1.0, cpu_time)))); // prevent division by zero

        // write out jobs 
        try {
            // giID - wait - runtime - userID - numPE - ram - arrival - queue
            out.writeStringWriterErr(pw2, gridlet_received.getGridletID() + "\t" + Math.max(0.0, (response - cpu_time))
                    + "\t" + cpu_time + "\t" + gi.getUser() + "\t" + gi.getNumPE() + "\t" + gi.getRam() + "\t" + gi.getRelease_date() + "\t" + gi.getQueue());
            String prob = "_";
            prob += ExperimentSetup.algID + "_" + ExperimentSetup.name;

            if (ExperimentSetup.estimates) {
                prob += "_estim";
            }
            if (!ExperimentSetup.estimates) {
                prob += "_exact";
            }
            if (!ExperimentSetup.anti_starvation) {
                prob += "_bez";
            } else {
                prob += "_stradani";
            }

            String line = gridlet_received.getGridletID() + "\t" + Math.round(gi.getRelease_date()) + "\t" + Math.round(Math.max(0.0, (response - cpu_time)) * 10) / 10.0
                    + "\t" + Math.round(cpu_time * 10) / 10.0 + "\t" + gi.getNumPE() + "\t" + gi.getRam() + "\t" + gi.getUser() + "\t" + gi.getQueue();

            //out.writeStringWriter(user_dir + "/jobs" + prob + ".csv", line.replace(".", ","));
            out.writeStringWriter(pw, line.replace(".", ","));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (int j = 0; j < resourceInfoList.size(); j++) {
            ResourceInfo ri = (ResourceInfo) resourceInfoList.get(j);
            if (gridlet_received.getResourceID() == ri.resource.getResourceID()) {
                // we lower the load of resource, update info about overall tardiness and exit cycle
                ri.lowerResInExec(gi);
                ri.prev_tard += g_tard;
                if (g_tard <= 0.0) {
                    ri.prev_score++;
                }
                break;
            }
        }

    }

    /**
     * Compute all results and stores them into a LinkedList.
     */
    public void computeResults(SchedulerData sd) {
        avail_time = GridSim.clock() * sd.getAv_PEs();
        avail_time -= sd.getFailureTime();
        double usage = Math.round((job_time / avail_time) * 10000.0);

        wavail_time = GridSim.clock() * sd.getWav_PEs();
        wavail_time -= sd.getWfailureTime();
        double wusage = Math.round((wjob_time / wavail_time) * 10000.0);

        // successfully completed jobs
        results.add(success);
        // required sched. time
        results.add(sd.getClock() / received);
        // makespan
        results.add(GridSim.clock());
        // classic machine usage
        results.add(usage / 100.0);

        //wait time plugin at the end of the method
        wait_time_global = Math.round((wait_time_global / received) * 100) / 100.0;
        results.add(wait_time_global);

        // negative deadline score
        // now it is failed
        results.add(failed);
        // total flow time
        results.add(flow_time / received);
        // weighted machine usage

        results.add(wusage / 100.0);
        // avg. runtime
        results.add(sd.getRuntime() / received);
        // avg. tardiness
        results.add(tardiness / received);
        //av. slowdown

        //calculated also as plugin at the end
        results.add(slowdown / received);

        //av. weighted response time
        results.add(awrt / sa_total);
        //av. weigted slowdown
        results.add(awsd / sa_total);
        results.add(sd.getSubmitted());

        // values reflecting only successfully completed jobs
        results.add(succ_flow / success);
        results.add(succ_wait / success);
        results.add(succ_slow / success);
        results.add(bound_succ_slow / success);
        backfilled = ExperimentSetup.backfilled;
        results.add(backfilled);

        //iterates all plugins and calculate their values and add them into results list
        for (Plugin pl : plugins) {
            //int index = Integer.parseInt(pl.getPluginConfiguration().get(PluginConfiguration.RESULT_HEADER));
            double result = pl.calculate(this, sd);
            results.add(result);
        }

    }

    /**
     * Resets all internal variables before new experiment starts.
     */
    public void reset() {
        this.success = 0;
        this.failed = 0;
        this.received = 0;
        this.job_time = 0.0;
        this.wjob_time = 0.0;
        this.tardiness = 0.0;
        this.succ_flow = 0.0;
        this.succ_slow = 0.0;
        this.bound_succ_slow = 0.0;
        this.succ_wait = 0.0;
        this.flow_time = 0.0;
        this.wait_time_global = 0.0;
        this.slowdown = 0.0;
        this.awrt = 0.0;
        this.awsd = 0.0;
        this.sa_total = 0.0;

        try {
            out.closeWriter(pw);
            out.closeWriter(pw2);
            out.closeWriter(pwc);
        } catch (IOException ex) {
            Logger.getLogger(ResultCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExperimentSetup.users.clear();
        ExperimentSetup.queues.clear();
    }

    private void clear(Double[] field) {
        for (int i = 0; i < field.length; i++) {
            field[i] = 0.0;
        }
    }

    /**
     * generate entry into CDF-like Graphs using the trace stored in Jobs.csv
     * file
     *
     * @param suff
     * @param job_count
     */
    private void generateCDFJobsStatistics(String suff, double job_count) {
        String line = "";
        Input r = new Input();

        try {
            out.closeWriter(pw2);
            out.closeWriter(pw);
            out.closeWriter(pwc);
        } catch (IOException ex) {
            Logger.getLogger(ResultCollector.class.getName()).log(Level.SEVERE, null, ex);
        }

        //pridan nazev slozky do puvodni cesty
        String fileName = user_dir + File.separator + ExperimentSetup.getDir(DirectoryLevel.ALGORITHM) + File.separator + "jobs(" + problem + "" + ExperimentSetup.algID + ").csv";
        //System.out.println("Nazev souboru:" + fileName);
        File file = new File(fileName);
        //System.out.println("Existuje soubor:" + file.exists());
        BufferedReader br = r.openFile(file);
        Double[] wt = new Double[1442];
        Double[] rt = new Double[2882];
        Double[] sd = new Double[1002];
        clear(wt);
        clear(rt);
        clear(sd);

        while (true) {
            try {
                line = br.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (line == null) {
                break;
            } else {
                String values[] = line.split("\t");
                //System.out.print(values[0] + ", ");
                double wait = Double.parseDouble(values[1]);
                double sld = Math.max(1.0, (Math.max(0.0, (wait + Double.parseDouble(values[2]))) / Math.max(1.0, Double.parseDouble(values[2]))));
                double resp = Math.max(0.0, (wait + Double.parseDouble(values[2])));
                // wait time in minutes
                wait = Math.round(wait / 60.0);
                resp = Math.round(resp / 60.0);
                Long inl = Math.round(wait);
                int index = Integer.valueOf(inl.intValue());

                Long inr = Math.round(resp);
                int rindex = Integer.valueOf(inr.intValue());

                Long sindexl = Math.round(sld);
                int sindex = Integer.valueOf(sindexl.intValue());

                // increase counter regarding the jobs
                if (index > 1440) {
                    wt[1441]++;
                } else {
                    wt[index]++;
                }

                if (rindex > 2880) {
                    rt[2881]++;
                } else {
                    rt[index]++;
                }

                // increase slowdown counter
                if (sindex > 1000) {
                    sd[1001]++;
                } else {
                    sd[sindex]++;
                }

                // handle users' fairness
                User u = ExperimentSetup.users.get(values[3]);
                wait = Double.parseDouble(values[1]);
                //System.out.println(ExperimentSetup.users.size()+" user = "+values[3]+" curr="+u);
                u.updateJobs(1.0);
                u.updateSlowdown(sld);
                u.updateWait(wait);
                //System.out.print("|"+values[4]+"|");
                u.updateRuntime(Double.parseDouble(values[2]) * Integer.parseInt(values[4]));
            }
        }

        //make the analysis and write it out to Graphs_*.csv
        line = suff;
        String sline = suff;
        String rline = suff;
        double cdf = 0.0;
        double scdf = 0.0;
        double rcdf = 0.0;

        // wait time CDF
        for (int i = 0; i < wt.length; i++) {
            double percent = wt[i] / job_count;
            cdf += percent;
            line += "\t" + cdf;
        }
        // response time CDF
        for (int i = 0; i < rt.length; i++) {
            double percent = rt[i] / job_count;
            rcdf += percent;
            rline += "\t" + rcdf;
        }
        // slowdown CDF
        for (int i = 0; i < sd.length; i++) {
            double spercent = sd[i] / job_count;
            scdf += spercent;
            sline += "\t" + scdf;
        }

        // write out job's result
        try {
            out.writeString(user_dir + "/WGraphs(" + problem + ").csv", line);
            out.writeString(user_dir + "/RGraphs(" + problem + ").csv", rline);
            out.writeString(user_dir + "/SGraphs(" + problem + ").csv", sline);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // delete job trace after each experiment
        try {
            out.deleteResults(user_dir + "/jobs(" + problem + "" + ExperimentSetup.algID + ").csv");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private String calculateUserStatistics() {

        String line = "";
        double[] nuwt = new double[Scheduler.users.size()];
        double[] tuwt = new double[Scheduler.users.size()];
        double[] turam = new double[Scheduler.users.size()];
        double[] tusa = new double[Scheduler.users.size()];
        double[] tujobs = new double[Scheduler.users.size()];
        double nwt = 0.0;

        // load known values
        for (int i = 0; i < Scheduler.users.size(); i++) {
            nuwt[i] = 0.0;
            tuwt[i] = Scheduler.final_total_uwt.get(i);
            turam[i] = Scheduler.final_total_uram.get(i);
            tusa[i] = Scheduler.final_users_CPUtime.get(i);
            tujobs[i] = Scheduler.users_jobs.get(i) + Scheduler.users_P_jobs.get(i);
        }
        // now tuwt and tusa stores both known and predicted values
        // now proceed with fairness computation        
        for (int i = 0; i < Scheduler.users.size(); i++) {
            nuwt[i] = tuwt[i] / Math.max(1.0, tusa[i]);
            nwt += nuwt[i];
        }
        nwt = nwt / (1.0 * Scheduler.users.size());
        int totj = 0;

        // calculate the sum of powers of average normalized wt - normalized user wt
        line += "user_id\ttuwt\tnuwt\ttusa\tjob_count\tavg_user_wait\tturam\tavg_user_ram\tavg_user_sq_area\n";
        for (int i = 0; i < Scheduler.users.size(); i++) {
            // to avoid decreasement of values when the power is computed we add 1.0 
            if (i < Scheduler.users.size() - 1) {
                line += Scheduler.users.get(i) + "\t" + tuwt[i] + "\t" + nuwt[i] + "\t" + tusa[i] + "\t" + tujobs[i] + "\t" + Math.round((tuwt[i] * 100.0) / tujobs[i]) / 100.0 + "\t" + turam[i] + "\t" + Math.round((turam[i] * 100.0) / tujobs[i]) / 100.0 + "\t" + Math.round((tusa[i] * 100.0) / tujobs[i]) / 100.0 + "\n";
            } else {
                line += Scheduler.users.get(i) + "\t" + tuwt[i] + "\t" + nuwt[i] + "\t" + tusa[i] + "\t" + tujobs[i] + "\t" + Math.round((tuwt[i] * 100.0) / tujobs[i]) / 100.0 + "\t" + turam[i] + "\t" + Math.round((turam[i] * 100.0) / tujobs[i]) / 100.0 + "\t" + Math.round((tusa[i] * 100.0) / tujobs[i]) / 100.0 + "";
            }
            totj += tujobs[i];
            //System.out.println("user"+i+" nuwt="+(Math.round(nuwt[i]*100.0))/100.0+" tusa="+Math.round(tusa[i]));            

        }
        return line;
    }

    /**
     * Sets the plugins to be used throughout the simulation.
     *
     * @param plugins the plugins to use
     */
    public void setPlugins(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    public void recordLongUserComplain(int gid, int userID, String user, double time, double factor, int job_count) {
        try {
            factor = Math.round(factor * 1000) / 1000.0;
            String output = gid + "\t" + Math.round(time) + "\t" + userID + "\t" + user + "\t" + factor + "\t" + job_count;
            // giID - time - userID - user - factor - job count
            out.writeStringWriterErr(pwc, output.replace(".", ","));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
