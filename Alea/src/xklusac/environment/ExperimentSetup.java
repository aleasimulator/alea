package xklusac.environment;
import xklusac.algorithms.schedule_based.BestGap;
import xklusac.algorithms.schedule_based.BF_CONS_Fair;
import xklusac.algorithms.schedule_based.optimization.GapSearch;
import xklusac.algorithms.schedule_based.optimization.TabuSearch;
import xklusac.algorithms.schedule_based.optimization.RandomSearch;
import xklusac.algorithms.schedule_based.optimization.WeightedRandomSearch;
import xklusac.algorithms.schedule_based.CONS;
import xklusac.algorithms.schedule_based.FairshareCONS;
import xklusac.algorithms.queue_based.multi_queue.EASY_Backfilling;
import xklusac.algorithms.queue_based.multi_queue.AggressiveBackfilling;
import xklusac.algorithms.queue_based.multi_queue.FairshareMetaBackfilling;
import xklusac.algorithms.queue_based.PBS_PRO;
import xklusac.algorithms.queue_based.multi_queue.FairshareFCFS;
import xklusac.algorithms.queue_based.multi_queue.FairshareOptimizedMetaBackfilling;
import xklusac.algorithms.queue_based.EDF;
import xklusac.algorithms.queue_based.multi_queue.Fairshare_EASY_Backfilling;
import xklusac.algorithms.queue_based.SJF;
import xklusac.algorithms.queue_based.multi_queue.FCFS;
import alea.core.Registration;

import eduni.simjava.Sim_system;
import java.io.IOException;
import java.util.*;
import gridsim.*;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
 * usage, slowdown, deadlines,...) are stored.<p> Alea 4.0 partially
 * supports <b>scheduling with RAM requirements</b> beside the common CPU
 * requests.<p>
 *
 * Most recent versions of <b>Alea</b> are available at: <a
 * href="http://www.fi.muni.cz/~xklusac/alea">http://www.fi.muni.cz/~xklusac/alea</a><br>
 * To run the simulation with Alea 4.0, Java 1.6 or newer is needed and
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
     * set to true if visualization should be shown. <br> Be carefull,
     * visualization requires some overhead which may slow down the simulation.
     * Use only for testing or if you want to obtain graphical output.
     */
    public static boolean visualize_schedule;
    
    /**
     * set true to use specific job requirements
     */
    static boolean reqs;
    /**
     * set true to use job runtime estimates
     */
    public static boolean estimates;
    /**
     * set true to use failure trace - if available
     */
    static boolean failures;
    /**
     * set true to use avg. job length as an runtime estimate
     */
    static boolean use_AvgLength;
    /**
     * set true to use last job runtime as a new runtime estimate
     */
    static boolean use_LastLength;
    /**
     * set true to use avg. perc. difference of all jobs to adjust runtime estimate
     */
    static boolean use_PercentageLength;
    /**
     * set true to min. perc. difference of last few jobs to adjust runtime estimate
     */
    static boolean use_MinPercentageLength;
    
    /**
     * set true to use on-demand LS-based optimization
     */
    static boolean useEventOpt;
    
    /**
     * defines whether heap is used to store schedule-data. Should be true, as
     * heap is faster than the default array.
     */
    static boolean use_heap;
    /**
     * random number generator seed
     */
    public static int rnd_seed;
    /**
     * defines how many times the default max-time job's limit has been used
     */
    public static int max_estim;
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
     * Arrival of the first job (all jobs are normalized to this value).
     */
    public static int firstArrival = -1;
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
     * defines whether to emulate user dissatisfaction in dynamic-workload simulations
     */
    public static boolean complain;
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
     * a reference to the job scheduler
     */
    public static Scheduler scheduler = null;
    
    /**
     * defines whether to use fairshare decay
     */
    public static boolean use_decay;
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
    public static int skipJob;
    /**
     * counter measuring the number of backfilled jobs
     */
    public static int backfilled;
    /**
     * counter measuring the number of backfilled jobs that actually must fit before a later job starts
     */
    public static int backfilled_cons;
    
    /**
     * the speed by which the schedule is updated in GUI
     */
    public static int schedule_repaint_delay;
    /**
     * A list containing reference to all local resource schedulers
     * (AllocationPolicy)
     */
    public static LinkedList local_schedulers = new LinkedList();
    /**
     * defines whether an anti-starvation technique based on resource
     * pre-allocation should be used
     */
    public static boolean anti_starvation;
    /**
     * defines whether jobs's resource specification can be adjusted to increase
     * througput. (So called nodespec packing option as seen in PBS Pro, etc.)
     */
    public static boolean resource_spec_packing;
    /**
     * defines whether several different queues in the system should be used.
     * Such queues must be specified in a seperate file, along with job and
     * machine descriptions.
     */
    public static boolean use_queues;
    
    public static ResultCollector result_collector = null;
    
    //private static String subDir;
    
    public static String alea_version = "4.0";
    
    /**
     * if several different queues in the system should are defined, this variable
     * defines whether they will be used separately (queue-by-queue in a defined priority
     * order) or they will only be used to guard queue-limits.
     */
    public static boolean by_queue;

    public static String data_sets;
    
    private static String[] dir = new String[4];
    
    private static String[] dirG = new String[5];
    
    public static LinkedList<Schedule_Visualizator> schedule_windows = new LinkedList();
    
    /**
     * Creates the path for storing simulation results.
     * 
     * @param level the depth of directory tree
     * 
     * @return the path to the appropriate directory at the specified level
     */
    public static String getDir(DirectoryLevel level) {
        String directory = dir[0];
        for (int i=1; i<level.ordinal(); i++) {
            directory += File.separator;
            directory += dir[i];
        }
        return directory;
    }
    
        public static String getDirG(DirectoryLevel level) {
        String directory = ExperimentSetup.getDir(DirectoryLevel.EXPERIMENT_ROOT);
        for (int i=2; i<level.ordinal(); i++) {
            directory += File.separator;
            directory += dirG[i];
        }
        return directory;
    }


    /*public static String getDir() {
        return subDir;
    }*/
    
    /**
     * This method returns the current date.
     * 
     * @return the date
     */
    public static String getDate() {
        Date d = new Date();
        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(d);
        return date;
    }

    public static int getFirstArrival() { return firstArrival; }

    /**
     * The main method - create all entities and start the simulation. <br> It
     * is also capable of multiple starts of the simulation with different setup
     * (machine count, job parameters, data sets).
     */
    public static void main(String[] args) {
        String user_dir = System.getProperty("user.dir");
        
        Registration.register();
        
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
        visualize_schedule = aCfg.getBoolean("visualize_schedule");        
        schedule_repaint_delay = aCfg.getInt("schedule_repaint_delay");
        
        use_heap = aCfg.getBoolean("use_heap");
        sld_tresh = aCfg.getDouble("sld_tresh");
        
        
        algID = 0;
        prevAlgID = -1;
        name = "";
        
        use_tsafrir = aCfg.getBoolean("use_tsafrir");
        use_speeds = aCfg.getBoolean("use_speeds");
        arrival_rate_multiplier = aCfg.getDouble("arrival_rate_multiplier");
        runtime_minimizer = aCfg.getDouble("runtime_minimizer");
        use_RAM = aCfg.getBoolean("use_RAM");
        use_fairshare_WAIT = aCfg.getBoolean("use_fairshare_WAIT");
        use_fairshare_RAM = aCfg.getBoolean("use_fairshare_RAM");
        use_fairshare = aCfg.getBoolean("use_fairshare");
        use_decay = aCfg.getBoolean("use_decay");
        multiply_sums = aCfg.getBoolean("multiply_sums");
        use_MAX = aCfg.getBoolean("use_MAX");
        use_SQRT = aCfg.getBoolean("use_SQRT");
        sum_multiplications = aCfg.getBoolean("sum_multiplications");
        useEventOpt = aCfg.getBoolean("useEventOpt");
        
        avail_RAM = 0.0;
        avail_CPUs = 0.0;
        
        backfilled = 0;
        backfilled_cons = 0;
        
        // set true to use different queues
        use_queues = aCfg.getBoolean("use_queues");
        by_queue = aCfg.getBoolean("by_queue");
        data_sets = aCfg.getString("data_set_dir");
        
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
        
        
        /*String user_dir = "";
        if (ExperimentSetup.meta) {
            
            user_dir = System.getProperty("user.dir");
            //user_dir = "/scratch/klusacek/" + path;
        } else {
            user_dir = System.getProperty("user.dir");
        }*/
        /*try {
            Output out = new Output();
            out.deleteResults(user_dir + "/jobs(" + problem + "" + ExperimentSetup.algID + ").csv");
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

        // stores references to animation windows
        LinkedList<Visualizator> windows = new LinkedList();
        // if true then create windows with graps.
        if (visualize) {
            Visualizator.createGUI(windows);
        }
        
        
        // if true then create windows with graps.
        if (visualize_schedule) {
            Schedule_Visualizator.createGUI(schedule_windows);
        }
        
        complain = aCfg.getBoolean("complain");

        // set true to use failures
        failures = aCfg.getBoolean("failures");
        // set true to use specific job requirements
        reqs = aCfg.getBoolean("reqs");
        // set true to use runtime estimates
        estimates = aCfg.getBoolean("estimates");
        // set true to refine estimates using job avg. length
        use_AvgLength = aCfg.getBoolean("use_AvgLength");
        // set true to use last job length as a new runtime estimate
        use_LastLength = aCfg.getBoolean("use_LastLength");
        use_PercentageLength = aCfg.getBoolean("use_PercentageLength");
        // the minimal length (in seconds) of gap in schedule since when the "on demand" optimization is executed
        gap_length = aCfg.getInt("gap_length");
        // the weight of the fairness criteria in objective function
        int weight_of_fairness[] = aCfg.getIntArray("weight_of_fairness");

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
        if (use_AvgLength) {
            problem += "-AvgL";
        }
        if (use_LastLength) {
            problem += "-LastL";
        }
        if (use_PercentageLength) {
            problem += "-PercL";
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
        

        // creates Result Collector
        result_collector = new ResultCollector(results, problem);
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        
        //folder for all results
        dir[0]="results";
        File resultsFile = new File(ExperimentSetup.getDir(DirectoryLevel.RESULT_ROOT));
        if (!resultsFile.exists()) {
            resultsFile.mkdir();
        }

        // creates file/folder for one setup
        dir[1] = getDate();
        File runDirF = new File(ExperimentSetup.getDir(DirectoryLevel.EXPERIMENT_ROOT));
        runDirF.mkdir();
        
        System.out.println("result root: " + runDirF.getPath());
        
        //copies the configuration file to the new folder
        File configurationF = aCfg.getFile();
        File destinationF = new File(ExperimentSetup.getDir(DirectoryLevel.EXPERIMENT_ROOT) + File.separator + aCfg.getFileName());
        //File destinationF = new File(dir[0] + File.separator + aCfg.getFileName());
        try {
            FileUtil.copyFile(configurationF, destinationF);
        } catch (IOException ex) {
            Logger.getLogger(ExperimentSetup.class.getName()).log(Level.WARNING, null, ex);
        }
        if (visualize || visualize_schedule) {
            //create folder graphs in experiment root directory
            dirG[2] = "graphs";
            //File graphs = new File(user_dir + File.separator + ExperimentSetup.getDir(DirectoryLevel.EXPERIMENT_ROOT) + File.separator + "graphs");
            File graphs = new File(ExperimentSetup.getDirG(DirectoryLevel.DATA_SET));
            graphs.mkdir();
            
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
        
        // this cycle selects data set from data_sets[] list
        for (int set = 0; set < data_sets.length; set++) {
            //creates new folder for each data set in the new setup folder
            String date = getDate();
            dir[2] = data_sets[set] + "_" + date;
            File dataSetDirF = new File(ExperimentSetup.getDir(DirectoryLevel.DATA_SET));
            dataSetDirF.mkdir();
            
            dirG[3] = data_sets[set] + "_" + date;
            File dataSetGraphs = new File(ExperimentSetup.getDirG(DirectoryLevel.ALGORITHM));
            dataSetGraphs.mkdir();
            //subDir = dir + File.separator + data_sets[set] + "_" + date;
            //File subDirF = new File(subDir);
            //subDirF.mkdir();
            
            if(data_sets[set].equals("sandia.swf")){
                failures = true;
            }
            String prob = problem;
            fair_weight = weight_of_fairness[set];
            max_estim = 0;
            result_collector.generateHeader(data_sets[set] + "_" + prob, pluginHeaders);
            prevAlgID = -1;

            // selects algorithm
            // write down the IDs of algorithm that you want to use (FCFS = 0, EDF = 1, EASY = 2, AgresiveBF = 3, CONS compression = 4, PBS PRO = 5, SJF = 6, FairShareFCFS = 7, 
            // (FCFS = 0, EDF = 1, EASY = 2, AgresiveBF = 3, CONS compression = 4, PBS PRO = 5, SJF = 6, FairShareFCFS = 7, 
            // FairShareMetaBackfilling = 8, FairShareCONS = 9, BestGap = 10, BestGap+RandomSearch = 11, FairShareOptimizedMetaBackfilling = 12
            // 18 = CONS+Tabu Search, 19 = CONS + Gap Search, 20 = CONS + RandomSearch, CONS no compression = 21,

            boolean use_anti_starvation[] = aCfg.getBooleanArray("use_anti_starvation");
            boolean estimateAVG[] = aCfg.getBooleanArray("estimateAVG");
            boolean estimateLAST[] = aCfg.getBooleanArray("estimateLAST");
            boolean estimatePERC[] = aCfg.getBooleanArray("estimatePERC");
            boolean estimateMPERC[] = aCfg.getBooleanArray("estimateMinPERC");
            boolean estimate[] = aCfg.getBooleanArray("estimate");
            boolean use_resource_spec_packing[] = aCfg.getBooleanArray("use_resource_spec_packing");
            int skip[] = aCfg.getIntArray("skip");

            int timeskip[] = aCfg.getIntArray("first_arrival");
            /*for (int i = 0; i < timeskip.length; i++) {
                Calendar myCal = Calendar.getInstance();
                myCal.setTimeInMillis(timeskip[i]*1000L);
                if (!(myCal.get(Calendar.HOUR_OF_DAY) == 0 && myCal.get(Calendar.MINUTE) == 0 && myCal.get(Calendar.SECOND) == 0 && myCal.get(Calendar.MILLISECOND) == 0)) {
                    throw new RuntimeException("Experiment starting time isn't midnight. (" + Integer.toString(myCal.get(Calendar.HOUR_OF_DAY)) + ":" +  
                            Integer.toString(myCal.get(Calendar.MINUTE)) + ":" + Integer.toString(myCal.get(Calendar.SECOND)) + ").");
                }
            }*/
            
            int algorithms[] = aCfg.getIntArray("algorithms");

            // select which algorithms from the algorithms[] list will be used.
            for (int sel_alg = 0; sel_alg < algorithms.length; sel_alg++) {

                anti_starvation = use_anti_starvation[sel_alg];
                
                use_AvgLength = estimateAVG[sel_alg];
                use_LastLength = estimateLAST[sel_alg];
                use_PercentageLength = estimatePERC[sel_alg];
                use_MinPercentageLength = estimateMPERC[sel_alg];
                estimates = estimate[sel_alg];
                
                resource_spec_packing = use_resource_spec_packing[sel_alg];
                skipJob = skip[set];
                firstArrival = timeskip[set];

                // reset values from previous iterations
                use_compresion = false;
                opt_alg = null;
                fix_alg = null;

                // get proper algorithm
                int alg = algorithms[sel_alg];
                int experiment_count = 1;
                backfilled = 0;
                backfilled_cons = 0;
                name = data_sets[set];
                algID = alg;
                if (sel_alg > 0) {
                    prevAlgID = algorithms[sel_alg - 1];
                }

                // used for output description
                String suff = "";
                
                // initialize the simulation - create the scheduler
                
                
                String scheduler_name = "Alea_3.0_scheduler";
                try {
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // true means tracing GridSim events
                    String[] exclude_from_file = {""};
                    String[] exclude_from_processing = {""};
                    String report_name = null;
                    GridSim.init(entities, calendar, trace_flag, exclude_from_file, exclude_from_processing, report_name);
                    int rnd = new Random().nextInt();
                    scheduler = new Scheduler(scheduler_name, baudRate, entities, results, alg, data_sets[set], total_gridlet[set], suff, windows, result_collector, sel_alg);
                } catch (Exception ex) {
                    Logger.getLogger(ExperimentSetup.class.getName()).log(Level.SEVERE, null, ex);
                }
                // this will set up the proper algorithm according to the algorithms[] list
                if (alg == 0) {
                    policy = new FCFS(scheduler);
                    suff = "FCFS";
                    if (resource_spec_packing) {
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
                    // Conservative backfilling (no RAM support)
                    use_compresion = true;
                    suff = "CONS+compression";
                }
                
                if (alg == 5) {
                    policy = new PBS_PRO(scheduler);
                    // do not use PBS-PRO on other than "metacentrum.mwf" data - not enough information is available.
                    suff = "PBS-PRO";
                }
                if (alg == 6) {
                    policy = new SJF(scheduler);
                    // Shortest Job First policy
                    suff = "SJF";
                }
                if (alg == 7) {
                    policy = new FairshareFCFS(scheduler);
                    // Fairshare ordered FCFS
                    suff = "FairShareFCFS";
                }
                if (alg == 8) {
                    policy = new FairshareMetaBackfilling(scheduler);
                    // Backfilling without a reservation
                    suff = "FairShareMetaBackfilling";
                    if (anti_starvation) {
                        suff += "-str";
                    }
                    if (resource_spec_packing) {
                        suff += "-pack";
                    }
                }
                if (alg == 12) {
                    policy = new FairshareOptimizedMetaBackfilling(scheduler);
                    // Backfilling without a reservation
                    suff = "FairShareOptimizedMetaBackfilling";
                    if (anti_starvation) {
                        suff += "-str";
                    }
                    if (resource_spec_packing) {
                        suff += "-pack";
                    }
                }
                if (alg == 9) {
                    policy = new FairshareCONS(scheduler);
                    // Conservative backfilling with fairshare (no RAM support)
                    use_compresion = true;
                    suff = "FairShareCONS+compr.";
                }

                if (alg == 10) {
                    policy = new BestGap(scheduler);
                    // Best-Gap backfill-like policy (no RAM support)
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
                    use_compresion = false;
                    policy = new CONS(scheduler);
                    opt_alg = new GapSearch();

                    if (useEventOpt) {
                        fix_alg = new GapSearch();
                        suff += "-EventOptGS";
                    }
                }
                if (alg == 20) {
                    suff = "CONS+RS(" + multiplicator + ")";
                    use_compresion = false;
                    policy = new CONS(scheduler);
                    opt_alg = new RandomSearch();
                    if (useEventOpt) {
                        fix_alg = new RandomSearch();
                        suff += "-EventOptRS";
                    }
                }

                if (alg == 201 || alg == 202 || alg == 203) {
                    suff = "CONS+RS(" + multiplicator + ")";
                    use_compresion = false;
                    policy = new CONS(scheduler);
                    opt_alg = new RandomSearch();
                    if (useEventOpt) {
                        fix_alg = new RandomSearch();
                        suff += "-EventOptRS";
                    }
                }
                if (alg == 204 || alg == 205 || alg == 206) {
                    suff = "CONS+RS(" + multiplicator + ")";
                    use_compresion = false;
                    policy = new CONS(scheduler);
                    opt_alg = new RandomSearch();
                    if (useEventOpt) {
                        fix_alg = new GapSearch();
                        suff += "-EventOptGS";
                    }
                }
                if (alg == 207 || alg == 208 || alg == 209) {
                    suff = "CONS+WRS(" + multiplicator + ")";
                    use_compresion = false;
                    policy = new CONS(scheduler);
                    opt_alg = new WeightedRandomSearch();
                    if (useEventOpt) {
                        fix_alg = new WeightedRandomSearch();
                        suff += "-EventOptWRS";
                    }
                }

                
                if (alg == 21) {
                    suff = "CONS-no-compress";
                    policy = new CONS(scheduler);
                }
                
                if (alg == 22) {
                    policy = new Fairshare_EASY_Backfilling(scheduler);
                    // fixed version of EASY Backfilling
                    suff = "EASY-Fair";
                }
                if (alg == 23) {
                    policy = new BF_CONS_Fair(scheduler);
                    // faster version of FairshareCONS (queue is not reshuffled every time)
                    // cannot be used when periodic fairshare update is enabled
                    // this update would break the detection of fairshare changes in BF_CONS_Fair
                    use_compresion = true;
                    suff = "CONS-Fair-compr.";
                }

                dirG[4] = (sel_alg+1) + "-" + suff;
                File algDirGraphs = new File(ExperimentSetup.getDirG(DirectoryLevel.GRAPHSALG));
                algDirGraphs.mkdir();               
                
                dir[3] = (sel_alg+1) + "-" + suff;
                File algDirF = new File(ExperimentSetup.getDir(DirectoryLevel.ALGORITHM));
                algDirF.mkdir();
                if(estimates){
                    suff+="-EST";
                }                
                
                if(use_AvgLength){
                    suff+="-AVG-L";
                }
                if(use_LastLength){
                    suff+="-LAST-L";
                }
                if(use_PercentageLength){
                    suff+="-PERC-L";
                }
                if(use_MinPercentageLength){
                    suff+="-MinPERC-L";
                }
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
                        System.out.println("The system has "+Math.round(avail_CPUs) + " CPUs and " + Math.round(avail_RAM/(1024*1024))+" GBs of RAM.");

                        // creates job loader
                        JobLoader job_loader = new JobLoader(job_loader_name, baudRate, total_gridlet[set], data_sets[set], maxPE, minPErating, maxPErating,
                                arrival_rate_multiplier, pass_count, m_loader.total_CPUs, estimates);

                        if (failures) {
                            // create machine failure loader
                            String failure_loader_name = data_sets[set] + "_FailureLoader";
                            FailureLoaderNew failure = new FailureLoaderNew(failure_loader_name, baudRate, data_sets[set], clusterNames, machineNames, 0);
                        }
                        // start the simulation
                        System.out.println("Starting simulation using Alea "+alea_version);
                        
                        GridSim.startGridSimulation();
                    } catch (Exception e) {
                        System.out.println("Unwanted errors happened!");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }

                    System.out.println("=============== END OF TEST " + pass_count + " ====================");
                    // reset inner variables of the simulator
                    if (visualize) {
                        Visualizator.saveImages();
                    }
                    if (visualize_schedule) {
                        Schedule_Visualizator.saveImages();
                    }
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
                    System.out.println("Max. estimate has been used = " + max_estim + " backfilled jobs = " + backfilled);
                    System.gc();                    
                }
            }
        }
        // end of the whole simulation
    }
} // end class
