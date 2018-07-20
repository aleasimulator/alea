/*
 * FailureLoader.java
 *
 *
 */
package xklusac.environment;

import eduni.simjava.Sim_event;
import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import xklusac.extensions.*;
import alea.core.AleaSimTags;

/**
 * Class FailureLoader<p>
 * This class is responsible for loading and generating of machine failures.
 *
 * @author Dalibor Klusacek
 */
public class FailureLoaderNew extends GridSim {

    /**
     * input
     */
    Input r = new Input();
    String folder_prefix = "";
    /**
     * buffered reader
     */
    BufferedReader br = null;
    /**
     * data set name
     */
    String data_set = "";
    String line;
    LinkedList clusterNames;
    LinkedList machineNames;
    int start_epoch = 0;
    long tot_f_t = 0;
    int subm_fails = 0;

    /**
     * Creates a new instance of FailureLoader
     */
    public FailureLoaderNew(String name, double baudRate, String data_set, LinkedList clusterNames, LinkedList machineNames, int version) throws Exception {
        super(name, baudRate);

        folder_prefix = System.getProperty("user.dir");

        if (data_set.equals("das2.gwf") && version == 1) {
            br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set + ".failuresL"));
            System.out.println(name + " loads " + folder_prefix + "/data-set/" + data_set + ".failuresL");
        } else if (data_set.equals("das2.gwf") && version == 2) {
            br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set + ".failuresM"));
            System.out.println(name + " loads " + folder_prefix + "/data-set/" + data_set + ".failuresM");
        } else {
            System.out.println("Opening job file at: " + ExperimentSetup.data_sets + "/" + data_set);
            br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set + ".failures"));
        }

        this.data_set = data_set;

    }

    /**
     * Reads failures from file and sends them to the specified machine
     * dynamically over time.
     */
    public void body() {
        super.gridSimHold(5.0);    // hold by 10 second

        if (data_set.equals("metacentrum.mwf") || data_set.equals("metacentrumE.mwf")) {
            // 1230768000 is the EPOCH time of 1.1.2009 00:00:00
            start_epoch = 1230768000;

        } else if (data_set.equals("das2.gwf")) {
            start_epoch = 1109087545;
        } else if (data_set.equals("grid5000.gwf")) {
            start_epoch = 1115812623;
        } else if (data_set.equals("sharcnet.gwf")) {
            start_epoch = 1135130133;
        } else if (data_set.equals("meta2008.mwf")) {
            start_epoch = 1199145600;
        } else if (data_set.equals("sandia.swf")) {
            start_epoch = 1007061086;
        }

        boolean ok = true;
        Scheduler.failure_time = 0;
        Scheduler.wfailure_time = 0;
        System.out.println("Reseting failure counters in Scheduler, now = " + Scheduler.failure_time + " and " + Scheduler.wfailure_time);
        while (ok) {

            Sim_event ev = new Sim_event();
            sim_get_next(ev);

            if (ev.get_tag() == AleaSimTags.EVENT_WAKE) {

                try {
                    line = br.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    ok = false;
                }
                if (line == null) {
                    break;
                }
                String[] values = null;
                if (data_set.equals("meta")) {
                    values = line.split(" ");
                } else {
                    values = line.split("\t");
                }
                //System.out.println("GO FOR >> "+line);
                // machine failure
                if (values.length > 3) {

                    double time = new Double(Integer.parseInt(values[0]) - start_epoch);
                    double duration = Integer.parseInt(values[2]);
                    if (duration < 60) {
                        super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, AleaSimTags.EVENT_WAKE);
                        continue;
                    }
                    String name = values[1];
                    String[] ids_str = values[3].split(" ");
                    int[] ids = new int[ids_str.length];

                    for (int i = 0; i < ids_str.length; i++) {
                        ids[i] = Integer.parseInt(ids_str[i]);
                    }

                    tot_f_t += duration * 2 * ids.length;

                    Failure failure = new Failure(name, time, duration, ids);
                    //System.out.println(name+" "+Integer.parseInt(values[1])+" "+duration+" "+ids[0]);
                    // to synchronize failure arrival wrt. the data set.
                    double delay = Math.max(0.0, (time - super.clock()));
                    // some time is needed to transfer this job to the scheduler, i.e., delay should be delay = delay - transfer_time. Fix this in the future.
                    System.out.println(Math.round(clock()) + ": Sending machine failure: " + values[0] + ", " + name + ", machID=" + ids[0] + ", delay = " + (Math.round(delay / 360.0)) / 10.0 + " hours, should arrive at: " + (Math.round(clock() + delay)));
                    subm_fails += ids.length;

                    //System.out.println(Math.round(clock()+delay)+": sending F: " + name +" machines:["+printIds(ids)+"], dur:"+duration);
                    super.sim_schedule(this.getEntityId(name), delay, AleaSimTags.FAILURE_MACHINE, failure);
                    //System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]+" t="+Math.round(clock()+delay));
                    super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);

                    // resource failure
                } else {
                    double time = new Double(Integer.parseInt(values[0]));
                    String name = values[1];
                    double duration = new Double(Integer.parseInt(values[2]));
                    // to synchronize failure arrival wrt. the data set.
                    double delay = Math.max(0.0, (time - super.clock()));
                    // some time is needed to transfer this job to the scheduler, i.e., delay should be delay = delay - transfer_time. Fix this in the future.
                    //System.out.println("Sending machine failure: " + values[0] + ", " + name + ", with delay = " + Math.round(delay / 3600));
                    super.sim_schedule(this.getEntityId(name), delay, AleaSimTags.FAILURE_INFO, duration);
                    super.sim_schedule(this.getEntityId(this.getEntityName()), delay, AleaSimTags.EVENT_WAKE);
                }
            } else if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
                ok = false;
                System.out.println("FailureLoader quits failure submission...");
                super.sim_schedule(this.getEntityId(this.getEntityName()), 0.0, GridSimTags.END_OF_SIMULATION);
            }

        }

        System.out.println("FailureLoader awaits new event (END)");

        Sim_event ev = new Sim_event();
        sim_get_next(ev);

        if (ev.get_tag() == GridSimTags.END_OF_SIMULATION) {
            System.out.println("++++++++++ Shuting down the " + data_set + "_FailureLoader. Tot. Fail time = " + tot_f_t + " | FAILS subm = " + subm_fails);
        } else {
            System.out.println("Different tag:" + ev.get_tag());
        }
        shutdownUserEntity();
        super.terminateIOEntities();

    }

    private String printIds(int[] ids) {
        String idss = "";
        for (int i = 0; i < ids.length; i++) {
            idss += ids[i] + ",";
        }
        return idss;
    }

    /**
     * Inner Class Failure<p>
     * Instance of this class represents one failure.
     *
     *
     */
    public class Failure {

        private String name;
        private int[] ids;
        private double time;
        private double duration;

        /**
         * Creates one failure
         */
        private Failure(String name, double time, double duration, int[] ids) {
            this.setName(name);
            this.setTime(time);
            this.setDuration(duration);
            this.setIds(ids);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int[] getIds() {
            return ids;
        }

        public void setIds(int[] ids) {
            this.ids = ids;
        }

        public double getTime() {
            return time;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }
    }
}
