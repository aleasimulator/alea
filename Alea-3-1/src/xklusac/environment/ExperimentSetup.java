package xklusac.environment;

import eduni.simjava.Sim_system;
import java.io.IOException;
import java.util.*;
import gridsim.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import xklusac.extensions.*;
import xklusac.extensions.Queue;
import xklusac.algorithms.*;
import xklusac.plugins.Plugin;
import xklusac.plugins.PluginConfiguration;
import xklusac.plugins.PluginFactory;

/**
 * Class ExperimentSetup <p> This is the main class. It creates all entities,
 * such as Scheduler, Resources and and performs the whole experiment. <br>It
 * allows to run multiple passes with different settings such as job parameters,
 * data-sets and algorithms. This parameters has to be properly set manually.
 * The results are printed on the screen and also stored in a text files when
 * the simulation is finished. Information about various objectives (machine
 * usage, slowdown, deadlines,...) are stored.<p> Alea 3.1beta partially
 * supports <b>scheduling with RAM requirements</b> beside the common CPU
 * requests.<p>
 *
 * Most recent versions of <b>Alea 3.1 final</b> are available at: <a
 * href="http://www.fi.muni.cz/~xklusac/alea">http://www.fi.muni.cz/~xklusac/alea</a><br>
 * To run the simulation with Alea 3.1 final, Java 1.6 or newer is needed and
 * the latest GridSim should be used.
 *
 * @author Dalibor Klusacek
 */
public class ExperimentSetup {
    
    private static AleaConfiguration aCfg;

    /**
     * bandwith
     */
    static double baudRate;
    /**
     * total count of Job Submission System
     */
    static int entities;
    /**
     * max. PE count at the "biggest" resource
     */
    static int maxPE;
    /**
     * min. PE rating of the slowest resource
     */
    static int minPErating = Integer.MAX_VALUE;
    static int maxPErating = 1;
    /**
     * names of clusters
     */
    static LinkedList clusterNames = new LinkedList();
    /**
     * names of machines
     */
    static LinkedList machineNames = new LinkedList();
    /**
     * set to true if visualization should be shown. <br> Be carefull,
     * visualization requires some overhead which may slow down the simulation.
     * Use only for testing or if you want to obtain graphical output.
     */
    static boolean visualize;
    /**
     * set true to use specific job requirements
     */
    static boolean reqs;
    /**
     * set true to use job runtime estimates
     */
    static boolean estimates;
    /**
     * set true to use failure trace - if available
     */
    static boolean failures;
    /**
     * set true to use avg. job length as an runtime estimate
     */
    static boolean useAvgLength;
    /**
     * set true to use last job runtime as a new runtime estimate
     */
    static boolean useLastLength;
    /**
     * set true to use on-demand LS-based optimization
     */
    static boolean useEventOpt;
    /**
     * defines where to look for sim. data - default is false
     */
    static boolean meta;
    /**
     * defines whether sim. data are outside of project folder.
     */
    static boolean data;
    /**
     * defines whether heap is used to store schedule-data. Should be true, as
     * heap is faster than the default array.
     */
    static boolean useHeap;
    /**
     * random number generator seed
     */
    static int rnd_seed;
    /**
     * defines how many times the default max-time job's limit has been used
     */
    static int max_estim;
    /**
     * allows to increase job's runtime by given factor
     */
    static int runtime_multiplicator;
    /**
     * auxiliary variable
     */
    static String path;
    /**
     * set of users in the system
     */
    static Hashtable<String, User> users = new Hashtable<String, User>();
    /**
     * set of queues in the system
     */
    public static Hashtable<String, Queue> queues = new Hashtable<String, Queue>();
    /**
     * multiplies the number of iterations of opt. algorithms
     */
    public static int multiplicator;
    /**
     * max time limit for optimization alg.
     */
    public static int time_limit;
    /**
     * max time limit for on-demand schedule optimization
     */
    public static int on_demand_time_limit;
    /**
     * defines the bounded slowdown's threshold (10 seconds typical)
     */
    public static double sld_tresh;
    /**
     * defines the minimal schedule gap's length that should trigger on-demand
     * optimization
     */
    static int gap_length;
    /**
     * ID of current sched. algorithm
     */
    static int algID;
    /**
     * ID of previous sched. algorithm
     */
    static int prevAlgID;
    /**
     * A string that denotes the experiment's setup (mainly data set name)
     */
    static String name;
    /**
     * the weight of fairness criterion in objective function
     */
    public static int fair_weight;
    /**
     * defines applied sched. algorithm
     */
    public static SchedulingPolicy policy = null;
    /**
     * defines applied optimization algorithm
     */
    public static OptimizationAlgorithm opt_alg = null;
    /**
     * defines applied opt. algorithm used to fix schedule after schedule
     * compression
     */
    public static OptimizationAlgorithm fix_alg = null;
    /**
     * defines whether to use schedule compression upon early job completion
     */
    public static boolean use_compresion;
    /**
     * defines whether to use Tsafrir's esimates (if available in the data set)
     */
    public static boolean use_tsafrir;
    /**
     * defines whether to use machine's speeds to adjust job execution time
     */
    public static boolean use_speeds;
    /**
     * can be used to compress job inter-arrival times (1.0 = original, 2.0 =
     * twice that fast)
     */
    public static double arrival_rate_multiplier;
    /**
     * can be used to decrease job's runtime by given factor
     */
    public static double runtime_minimizer;
    /**
     * defines whether job's RAM requirements should be followed
     */
    public static boolean use_RAM;
    /**
     * defines whether use fairshare based on normalized wait time
     * (tot_wait/tot_runtime)
     */
    public static boolean use_fairshare_WAIT;
    /**
     * defines whether to use fairshare counting in RAM as well as CPU time
     */
    public static boolean use_fairshare_RAM;
    /**
     * defines whether to use fairshare at all
     */
    public static boolean use_fairshare;
    /**
     * defines whether to multiply sum of CPU and RAM in fairhshare
     */
    public static boolean multiply_sums;
    /**
     * defines whether to use MAX of CPU and RAM usage in fairshare
     */
    public static boolean use_MAX;
    /**
     * defines whether to use SQRT of CPU and RAM usage in fairshare
     */
    public static boolean use_SQRT;
    /**
     * defines whether to sum multiplications of CPU and RAM in fairhshare
     */
    public static boolean sum_multiplications;
    /**
     * defines total available RAM in the system
     */
    public static double avail_RAM;
    /**
     * defines total available CPUs in the system
     */
    public static double avail_CPUs;
    /**
     * defines how many jobs should be skipped in the data set
     */
    public static int skip;
    /**
     * counter measuring the number of backfilled jobs
     */
    public static int backfilled;
    /**
     * A list containing reference to all local resource schedulers
     * (AllocationPolicy)
     */
    public static LinkedList local_schedulers = new LinkedList();
    /**
     * defines whether an anti-starvation technique based on resource
     * pre-allocation should be used
     */
    public static boolean use_anti_starvation;
    /**
     * defines whether jobs's resource specification can be adjusted to increase
     * througput. (So called nodespec packing option as seen in PBS Pro, etc.)
     */
    public static boolean use_resource_spec_packing;
    /**
     * defines whether several different queues in the system should be used.
     * Such queues must be specified in a seperate file, along with job and
     * machine descriptions.
     */
    public static boolean use_queues;
    
    //private static String subDir;
    
    private static String[] dir = new String[3];
    
    /**
     * Creates the path.
     * @param level where the results should be stored
     * @return the path to the directory at the specified level
     */
    public static String getDir(int level) {
        String directory = dir[0];
        for (int i=1; i<level; i++) {
            directory += File.separator;
            directory += dir[i];
        }
        return directory;
    }

    /*public static String getDir() {
        return subDir;
    }*/
    
    /**
     * This method returns date in the specified format.
     * @return the date
     */
    public static String getDate() {
        Date d = new Date();
        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(d);
        return date;
    }
    
    /**
     * The main method - create all entities and starts the simulation. <br> It
     * is also capable of multiple starts of the simulation with different setup
     * (machine count, job parameters, data sets).
     */
    public static void main(String[] args) {
        try {
            aCfg = new AleaConfiguration();
        } catch (IOException e) { 
            System.err.println("Could not load configuration file!"+e);
            return;
        }
        
        baudRate = aCfg.getDouble("baudRate");
        entities = aCfg.getInt("entities");
        
        maxPE = 1;
        
        visualize = aCfg.getBoolean("visualize");
        data = aCfg.getBoolean("data");
        useHeap = aCfg.getBoolean("useHeap");
        sld_tresh = aCfg.getDouble("sld_tresh");
        
        algID = 0;
        prevAlgID = -1;
        name = "";
        
        use_compresion = aCfg.getBoolean("use_compresion");
        use_tsafrir = aCfg.getBoolean("use_tsafrir");
        use_speeds = aCfg.getBoolean("use_speeds");
        arrival_rate_multiplier = aCfg.getDouble("arrival_rate_multiplier");
        runtime_minimizer = aCfg.getDouble("runtime_minimizer");
        use_RAM = aCfg.getBoolean("use_RAM");
        use_fairshare_WAIT = aCfg.getBoolean("use_fairshare_WAIT");
        use_fairshare_RAM = aCfg.getBoolean("use_fairshare_RAM");
        use_fairshare = aCfg.getBoolean("use_fairshare");
        multiply_sums = aCfg.getBoolean("multiply_sums");
        use_MAX = aCfg.getBoolean("use_MAX");
        use_SQRT = aCfg.getBoolean("use_SQRT");
        sum_multiplications = aCfg.getBoolean("sum_multiplications");
        useEventOpt = aCfg.getBoolean("useEventOpt");
        
        avail_RAM = 0.0;
        avail_CPUs = 0.0;
        
        skip = aCfg.getInt("skip");
        
        backfilled = 0;
        
        use_anti_starvation = aCfg.getBoolean("use_anti_starvation");
        use_resource_spec_packing = aCfg.getBoolean("use_resource_spec_packing");
        // set true to use different queues
        use_queues = aCfg.getBoolean("use_queues");
        
        
        // if required - start the graphical output using -v parameter
        if (args.length > 0) {
            if (args[0].equals("-v")) {
                visualize = true;
            }
        }

        // list of results
        LinkedList results = new LinkedList();

        // data set name(s) are stored in this list. Typically, the data are expected to be in a "$PATH/data-set/" directory, where $PATH is the path to where the Alea directory is.
        // Therefore, this directory should contain both ./Alea and ./data-set directories. Files describing machines should be placed in a file named e.g., "metacentrum.mwf.machines".
        // Similarly machine failures (if simulated) should be placed in a file called e.g., "metacentrum.mwf.failures".
        // Please read carefully the copyright note when using public workload traces!
        String data_sets[] = aCfg.getStringArray("data_sets");
        // number of gridlets in data set
        int total_gridlet[] = aCfg.getIntArray("total_gridlet");

        // stores references to animation windows
        LinkedList<Visualizator> windows = new LinkedList();
        // if true then create windows with graps.
        if (visualize) {
            Visualizator.createGUI(windows);
        }

        // set true to use failures
        failures = aCfg.getBoolean("failures");
        // set true to use specific job requirements
        reqs = aCfg.getBoolean("reqs");
        // set true to use runtime estimates
        estimates = aCfg.getBoolean("estimates");
        // set true to refine estimates using job avg. length
        useAvgLength = aCfg.getBoolean("useAvgLength");
        // set true to use last job length as a new runtime estimate
        useLastLength = aCfg.getBoolean("useLastLength");
        // the minimal length (in seconds) of gap in schedule since when the "on demand" optimization is executed
        gap_length = aCfg.getInt("gap_length");
        // the weigh of fairness criterion
        fair_weight = aCfg.getInt("fair_weight");
        // the weight of the fairness criteria in objective function
        int fairw[] = aCfg.getIntArray("fairw");

        //defines the name format of output files
        String problem = "Result";
        if (!failures && !reqs) {
            problem += "Basic";
        }
        if (reqs) {
            problem += "R-";
        }
        if (failures) {
            problem += "F";
        }
        if (estimates) {
            problem += "-Estim";
        } else {
            problem += "-Exact";
        }
        if (useAvgLength) {
            problem += "-AvgL";
        }
        if (useLastLength) {
            problem += "-LastL";
        }
        if (useEventOpt) {
            problem += "-EventOpt";
        }

        // multiply the number of iterations of optimization techniques
        multiplicator = aCfg.getInt("multiplicator");
        // time limit for optimization after early job completion
        on_demand_time_limit = aCfg.getInt("on_demand_time_limit");
        // time limit for optimization during regular periodic optimization
        time_limit = aCfg.getInt("time_limit");
        // factor by which the previous runtime is increased when historical estimates are used
        runtime_multiplicator = aCfg.getInt("runtime_multiplicator");


        // used only when executed on a real cluster (do not change)
        path = aCfg.getString("path");
        meta = aCfg.getBoolean("meta");
        if (meta) {
            data = false;
            String date = "-" + new Date().toString();
            date = date.replace(" ", "_");
            date = date.replace("CET_", "");
            date = date.replace(":", "-");
            System.out.println(date);
            problem += date;
        }

        String user_dir = "";
        if (ExperimentSetup.meta) {
            user_dir = "/scratch/klusacek/" + path;
        } else {
            user_dir = System.getProperty("user.dir");
        }
        /*try {
            Output out = new Output();
            out.deleteResults(user_dir + "/jobs(" + problem + "" + ExperimentSetup.algID + ").csv");
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

        // creates Result Collector
        ResultCollector result_collector = new ResultCollector(results, problem);
        
        // creates file/folder for one setup
        dir[0] = getDate();
        File runDirF = new File(dir[0]);
        runDirF.mkdir();
        
        //copies the configuration file to the new folder
        File configurationF = aCfg.getFile();
        File destinationF = new File(dir[0] + File.separator + aCfg.getFileName());
        try {
            FileUtil.copyFile(configurationF, destinationF);
        } catch (IOException ex) {
            Logger.getLogger(ExperimentSetup.class.getName()).log(Level.WARNING, null, ex);
        }
        
        String[] pluginsString = aCfg.getStringArray("plugins");
        List<Map<String, String>> pluginConfigurations = new ArrayList<Map<String, String>>();
        String[] pluginHeaders = new String[pluginsString.length];
        
        //loads pluginConfigurations from config file
        for (int i=0; i<pluginsString.length; i++) {
            Map<String, String> plugincfg = aCfg.getPluginConfiguration(i);
            String header = plugincfg.get(PluginConfiguration.RESULT_HEADER);
            pluginHeaders[i] = header;
            pluginConfigurations.add(plugincfg);      
        }
        /*Map<String, String> plugin0cfg = new HashMap<String, String>();
        Map<String, String> plugin1cfg = new HashMap<String, String>();
        plugin0cfg.put(PluginConfiguration.RESULT_INDEX, "4");
        plugin1cfg.put(PluginConfiguration.RESULT_INDEX, "10");
        pluginConfigurations.add(plugin0cfg);
        pluginConfigurations.add(plugin1cfg);*/
        
        // this cycle selects data set from data_sets[] list
        for (int set = 0; set < data_sets.length; set++) {
            //creates new folder for each data set in the new setup folder
            String date = getDate();
            dir[1] = data_sets[set] + "_" + date;
            File dataSetDirF = new File(ExperimentSetup.getDir(2));
            dataSetDirF.mkdir();
            //subDir = dir + File.separator + data_sets[set] + "_" + date;
            //File subDirF = new File(subDir);
            //subDirF.mkdir();
            
            String prob = problem;
            fair_weight = fairw[set];
            max_estim = 0;
            result_collector.generateHeader(data_sets[set] + "_" + prob, pluginHeaders);
            prevAlgID = -1;

            // selects algorithm
            // write down the IDs of algorithm that you want to use (FCFS = 0, EDF = 1, EASY = 2, AgresiveBF = 3, CONS compression = 4, PBS PRO = 5, SJF = 6, FairShareFCFS = 7, 
            // FairShareMetaBackfilling = 8, FairShareCONS = 9, BestGap = 10, BestGap+RandomSearch = 11, 18 = CONS+Tabu Search, 19 = CONS + Gap Search, 20 = CONS + RandomSearch, CONS no compression = 21,

            boolean stradej[] = aCfg.getBooleanArray("stradej");
            boolean packuj[] = aCfg.getBooleanArray("packuj");
            int skipuj[] = aCfg.getIntArray("skipuj");
            int algorithms[] = aCfg.getIntArray("algorithms");

            // select which algorithms from the algorithms[] list will be used.
            for (int sel_alg = 0; sel_alg < algorithms.length; sel_alg++) {

                use_anti_starvation = stradej[sel_alg];
                use_resource_spec_packing = packuj[sel_alg];
                skip = skipuj[set];

                // reset values from previous iterations
                use_compresion = false;
                opt_alg = null;
                fix_alg = null;

                // get proper algorithm
                int alg = algorithms[sel_alg];
                int experiment_count = 1;
                backfilled = 0;
                name = data_sets[set];
                algID = alg;
                if (sel_alg > 0) {
                    prevAlgID = algorithms[sel_alg - 1];
                }

                // used for output description
                String suff = "";
                // initialize the simulation - create the scheduler
                
                Scheduler scheduler = null;
                String scheduler_name = "Alea_3.0_scheduler";
                try {
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // true means tracing GridSim events
                    String[] exclude_from_file = {""};
                    String[] exclude_from_processing = {""};
                    String report_name = null;
                    GridSim.init(entities, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);
                    scheduler = new Scheduler(scheduler_name, baudRate, entities, results, alg, data_sets[set], total_gridlet[set], suff, windows, result_collector, sel_alg);
                } catch (Exception ex) {
                    Logger.getLogger(ExperimentSetup.class.getName()).log(Level.SEVERE, null, ex);
                }
                // this will set up the proper algorithm according to the algorithms[] list
                if (alg == 0) {
                    policy = new FCFS(scheduler);
                    suff = "FCFS";
                    if (use_resource_spec_packing) {
                        suff += "-pack";
                    }
                }
                if (alg == 1) {
                    policy = new EDF(scheduler);
                    suff = "EDF";
                }
                if (alg == 2) {
                    policy = new EASY_Backfilling(scheduler);
                    // fixed version of EASY Backfilling
                    suff = "EASY";
                }
                if (alg == 3) {
                    policy = new AggressiveBackfilling(scheduler);
                    // Backfilling without a reservation
                    suff = "Aggressive-Backfill";
                }
                if (alg == 4) {
                    policy = new CONS(scheduler);
                    use_compresion = true;
                    suff = "CONS+compression";
                }
                // do not use PBS-PRO on other than "metacentrum.mwf" data - not enough information is available.
                if (alg == 5) {
                    policy = new PBS_PRO(scheduler);
                    suff = "PBS-PRO";
                }
                if (alg == 6) {
                    policy = new SJF(scheduler);
                    // Backfilling without a reservation
                    suff = "SJF";
                }
                if (alg == 7) {
                    policy = new FairshareFCFS(scheduler);
                    // Backfilling without a reservation
                    suff = "FairShareFCFS";
                }
                if (alg == 8) {
                    policy = new FairshareMetaBackfilling(scheduler);
                    // Backfilling without a reservation
                    suff = "FairShareMetaBackfilling";
                    if (use_anti_starvation) {
                        suff += "-str";
                    }
                    if (use_resource_spec_packing) {
                        suff += "-pack";
                    }
                }
                if (alg == 12) {
                    policy = new FairshareOptimizedMetaBackfilling(scheduler);
                    // Backfilling without a reservation
                    suff = "FairShareOptimizedMetaBackfilling(o4)";
                    if (use_anti_starvation) {
                        suff += "-str";
                    }
                    if (use_resource_spec_packing) {
                        suff += "-pack";
                    }
                }
                if (alg == 9) {
                    policy = new FairshareCONS(scheduler);
                    use_compresion = true;
                    suff = "FairShareCONS+compr.";
                }

                if (alg == 10) {
                    policy = new BestGap(scheduler);
                    suff = "BestGap";
                }
                if (alg == 11) {
                    suff = "BestGap+RandSearch(" + multiplicator + ")";
                    policy = new BestGap(scheduler);
                    opt_alg = new RandomSearch();
                    if (useEventOpt) {
                        fix_alg = new GapSearch();
                        suff += "-EventOptGS";
                    }
                }

                if (alg == 18) {
                    suff = "CONS+TS(" + multiplicator + ")";
                    policy = new CONS(scheduler);
                    opt_alg = new TabuSearch();

                    if (useEventOpt) {
                        fix_alg = new TabuSearch();
                        suff += "-EventOptTS";
                    }
                }

                if (alg == 19) {
                    suff = "CONS+GS(" + multiplicator + ")";
                    policy = new CONS(scheduler);
                    opt_alg = new GapSearch();

                    if (useEventOpt) {
                        fix_alg = new GapSearch();
                        suff += "-EventOptGS";
                    }
                }
                if (alg == 20) {
                    suff = "CONS+RandSearch(" + multiplicator + ")";
                    policy = new CONS(scheduler);
                    opt_alg = new RandomSearch();
                    if (useEventOpt) {
                        fix_alg = new RandomSearch();
                        suff += "-EventOptRS";
                    }
                }
                if (alg == 21) {
                    suff = "CONS-no-compress";
                    policy = new CONS(scheduler);
                }
                
                dir[2] = suff;
                File algDirF = new File(ExperimentSetup.getDir(3));
                System.out.println(getDir(3));
                algDirF.mkdir();
                
                result_collector.deleteSchedResults(suff);//originally in Scheduler constructor
                
                System.out.println("Now scheduling " + total_gridlet[set] + " jobs by: " + suff + ", using " + data_sets[set] + " data set.");

                suff += "@" + data_sets[set];
                
                // this cycle may be used when some modifications of one data set are required in multiple runs of Alea 3.0 over same data-set.
                for (int pass_count = 1; pass_count <= experiment_count; pass_count++) {
                    List<Plugin> plugins = new ArrayList<Plugin>();
                    //create instances of plugins and add them to the list
                    //if the plugin implementation is outside this project, the classpath should be entered. If not, we suppose it is in the package plugins of this project.
                    for (int i = 0; i< pluginsString.length; i++) {
                        String pluginString = pluginsString[i];
                        if (!pluginString.contains(".")) {
                            pluginString = "xklusac.plugins." + pluginString;
                        }
                        Plugin pl = PluginFactory.createPlugin(pluginString);
                        pl.init(pluginConfigurations.get(i));
                        plugins.add(pl);
                    }
                    result_collector.setPlugins(plugins);
                    
                    avail_RAM = 0;
                    avail_CPUs = 0;

                    try {
                        // creates entities
                        String job_loader_name = data_sets[set] + "_JobLoader";
                        if (use_queues) {
                            // queues from data file
                            QueueLoader q_loader = new QueueLoader(data_sets[set]);
                        } else {
                            // default queue
                            Scheduler.all_queues.addLast(new LinkedList<GridletInfo>());
                        }
                        // creates all grid resources
                        MachineLoader m_loader = new MachineLoader(10000, 3.0, data_sets[set]);
                        rnd_seed = sel_alg;
                        System.out.println(avail_CPUs + " CPUs and RAM = " + avail_RAM);

                        // creates job loader
                        JobLoader job_loader = new JobLoader(job_loader_name, baudRate, total_gridlet[set], data_sets[set], maxPE, minPErating, maxPErating,
                                arrival_rate_multiplier, pass_count, m_loader.total_CPUs, estimates);

                        if (failures) {
                            // create machine failure loader
                            String failure_loader_name = data_sets[set] + "_FailureLoader";
                            FailureLoaderNew failure = new FailureLoaderNew(failure_loader_name, baudRate, data_sets[set], clusterNames, machineNames, 0);
                        }
                        // start the simulation
                        System.out.println("Starting simulation using Alea 3.1 final");
                        
                        GridSim.startGridSimulation();
                    } catch (Exception e) {
                        System.out.println("Unwanted errors happened!");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }

                    System.out.println("=============== END OF TEST " + pass_count + " ====================");
                    // reset inner variables of the simulator
                    Scheduler.load = 0.0;
                    Scheduler.classic_load = 0.0;
                    Scheduler.max_load = 0.0;
                    Scheduler.classic_activePEs = 0.0;
                    Scheduler.classic_availPEs = 0.0;
                    Scheduler.activePEs = 0.0;
                    Scheduler.availPEs = 0.0;
                    Scheduler.requestedPEs = 0.0;
                    Scheduler.last_event = 0.0;
                    Scheduler.start_event = -10.0;
                    Scheduler.runtime = 0.0;

                    // reset internal SimJava variables to start new experiment with different job/gridlet setup
                    Sim_system.setInComplete(true);
                    // store results
                    result_collector.generateResults(suff, experiment_count);
                    result_collector.reset();
                    results.clear();
                    System.out.println("Max. estim has been used = " + max_estim + " backfilled jobs = " + backfilled);
                    System.gc();
                    
                }
            }
        }
        // end of the whole simulation
    }
} // end class