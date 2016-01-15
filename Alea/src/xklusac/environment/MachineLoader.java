/*
 * MachineLoader.java
 *
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package xklusac.environment;

import gridsim.*;
import java.io.BufferedReader;
import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;
import xklusac.extensions.Input;

/**
 * Class MachineLoader<p> Creates GridResources according to specified data set.
 *
 * @author Dalibor Klusacek
 */
public class MachineLoader {

    private double bandwidth;
    private double cost;
    private String data_set;
    public int total_CPUs = 0;

    /**
     * Creates a new instance of MachineLoader
     */
    public MachineLoader(double bandwidth, double cost, String data_set) {
        System.out.println("Starting Machine Loader ...");
        this.bandwidth = bandwidth;
        this.cost = cost;
        this.data_set = data_set;
        this.total_CPUs = 0;
        init(data_set);
    }

    /**
     * Based on the type of workload, machines and Grid resources are generated
     * here.
     */
    private void init(String set) {

        if (set.contains("gwf")) {
            createGridResources(bandwidth, cost, set);
        } else if (set.contains("swf")) {
            createGridResources(bandwidth, cost, set);
        } else if (set.contains("mwf")) {
            createMETAGridResources(bandwidth, cost, set);
        } else if (set.contains("pwf")) {
            createPisaGridResources(bandwidth, cost, set);
        } else if (set.contains("dyn")) {
            createGridResources(bandwidth, cost, set);
        } else {
            System.out.println("Wrong machine workload format or file extension (gwf.machines, swf.machines, mwf.machines, pwf.machines, ai.machines)");
        }

    }

    /**
     * Creates Grid resources
     */
    protected void createGridResources(double bandwidth, double cost, String data_set) {
        // read data-set from file
        LinkedList lines = new LinkedList();
        Input r = new Input();

        BufferedReader br = null;

        br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set + ".machines"));
        System.out.println("Opening: " + ExperimentSetup.data_sets + "/" + data_set + ".machines");
        r.getLines(lines, br);
        r.closeFile(br);
        int name_id = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;
        // unused now
        String cpu_ids = "0,1,2,3,4,5";

        // create resources and machines from file
        for (int j = 0; j < lines.size(); j++) {

            String[] values = ((String) lines.get(j)).split("\t");
            //System.out.println(lines.get(j));
            int id = Integer.parseInt(values[0]);
            long ram = 1024;
            if (values.length > 5) {
                //ram in KB
                ram = Long.parseLong(values[5]);
            }
            int totalMachine = Integer.parseInt(values[2]);
            int totalPE = Integer.parseInt(values[3]);
            int peRating = Integer.parseInt(values[4]);
            String name = values[1];

            // for JobLoader's purposes
            max = totalPE * totalMachine;
            if (max > ExperimentSetup.maxPE) {
                ExperimentSetup.maxPE = max;
            }
            // for JobLoader's purposes
            min = peRating;
            if (min < ExperimentSetup.minPErating) {
                ExperimentSetup.minPErating = min;
            }
            if (peRating > ExperimentSetup.maxPErating) {
                ExperimentSetup.maxPErating = peRating;
            }


            //    A Machine contains one or more PEs or CPUs. Therefore, should
            //    create an object of PEList to store these PEs before creating
            //    a Machine.
            MachineList mList = new MachineList();
            for (int m = 0; m < totalMachine; m++) {

                PEList peList = new PEList();
                for (int k = 0; k < totalPE; k++) {
                    // need to store PE id and MIPS Rating
                    peList.add(new PE(k, peRating));
                }

                mList.add(new MachineWithRAM(m, peList, ram));


            }


            //    Create a ResourceCharacteristics object that stores the
            //    properties of a Grid resource: architecture, OS, list of
            //    Machines allocation policy: time- or space-shared, time zone
            //    and its price (G$/PE time unit).

            // to-do: read this from file. These values are sharcnet specific
            String arch = "Pentium 4";      // system architecture e.g. Xeon, Opteron, Pentium3

            String os = "Scientific Linux";          // operating system e.g. Linux, Debian

            double time_zone = 0.0;         // time zone this resource located

            String properties = "";

            //name = name;
            name_id++;

            ComplexResourceCharacteristics resConfig = new ComplexResourceCharacteristics(
                    arch, os, mList, ResourceCharacteristics.SPACE_SHARED, time_zone, cost, ram, properties, cpu_ids);




            // Finally, we need to create a ComplexGridResource object.
            long seed = 11L * 13 * 17 * 19 * 23 + 1;
            double peakLoad = 0.0;       // the resource load during peak hour

            double offPeakLoad = 0.0;    // the resource load during off-peak hr

            double holidayLoad = 0.0;    // the resource load during holiday

            // incorporates weekends so the grid resource is on 7 days a week
            LinkedList Weekends = new LinkedList();
            Weekends.add(new Integer(Calendar.SATURDAY));
            Weekends.add(new Integer(Calendar.SUNDAY));

            // incorporates holidays. However, no holidays are set in this example
            LinkedList Holidays = new LinkedList();
            AllocPolicy apolicy = null;
            try {
                // this is usefull because we can define resources internal scheduling system (FCFS/RR/BackFilling,FairQueuing...)
                if (ExperimentSetup.use_RAM) {
                    AdvancedSpaceSharedWithRAM policy = null;
                    policy = new AdvancedSpaceSharedWithRAM(name, "AdvancedSpaceSharedPolicyWithRAM", resConfig);
                    ExperimentSetup.local_schedulers.add(policy);
                    apolicy = policy;

                } else {
                    AdvancedSpaceShared policy = null;
                    policy = new AdvancedSpaceShared(name, "AdvancedSpaceSharedPolicy", resConfig);
                    ExperimentSetup.local_schedulers.add(policy);
                    apolicy = policy;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                ResourceCalendar resCalendar = new ResourceCalendar(time_zone,
                        peakLoad, offPeakLoad, holidayLoad, Weekends,
                        Holidays, seed);

                ComplexGridResource gridRes = new ComplexGridResource(name, bandwidth, resConfig,
                        resCalendar, apolicy);

                ExperimentSetup.avail_CPUs += gridRes.resource_.getNumPE();

                MachineList machines = gridRes.resource_.getMachineList();
                for (int i = 0; i < machines.size(); i++) {
                    MachineWithRAM machine = (MachineWithRAM) machines.get(i);
                    ExperimentSetup.avail_RAM += machine.getRam();

                }


            } catch (Exception e) {
                System.out.println("Error in creating GridResource.");
                System.out.println(e.getMessage());
            }


        }
    }

    /**
     * Creates Grid resources
     */
    protected void createMETAGridResources(double bandwidth, double cost, String data_set) {
        // read data-set from file
        LinkedList<String> lines = new LinkedList();
        Input r = new Input();
        BufferedReader br = null;

        br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set + ".machines"));
        r.getLines(lines, br);
        r.closeFile(br);

        LinkedList<String> cpu_types = new LinkedList();
        LinkedList<Double> cpu_bench = new LinkedList();
        LinkedList<Double> cpu_speed = new LinkedList();

        cpu_types.add("Pentium3");
        cpu_types.add("AthlonMP");
        cpu_types.add("Xeon");
        cpu_types.add("Xeon");
        cpu_types.add("Opteron");
        cpu_types.add("Itanium2");
        cpu_types.add("Pentium4");
        cpu_types.add("unspecified");
        cpu_bench.add((1.0));
        cpu_bench.add((1.771666667));
        cpu_bench.add((2.88));
        cpu_bench.add((3.643333333));
        cpu_bench.add((4.743333333));
        cpu_bench.add((6.844));
        cpu_bench.add((1.79));
        cpu_bench.add((1.0));
        cpu_speed.add(1000.0);
        cpu_speed.add(1800.0);
        cpu_speed.add(2400.0);
        cpu_speed.add(3060.0);
        cpu_speed.add(2200.0);
        cpu_speed.add(1500.0);
        cpu_speed.add(1000.0);
        cpu_speed.add(1000.0);

        String[] last_machine = null;
        int machine_id = 0;
        MachineList mList = new MachineList();
        //String name = "Cluster_";
        String name = "";
        int name_id = 0;
        String cpu_ids = "";
        // list of machine names
        LinkedList<String> names = new LinkedList();

        // create machines from file
        for (int j = 0; j < lines.size(); j++) {

            String[] values = ((String) lines.get(j)).split("\t");
            name = values[1];
            int MachCount = Integer.parseInt(values[7]);

            String PEs[] = values[9].split(",");
            String PEids[] = values[10].split(",");
            // remeber largest PE id.
            for (int id = 0; id < PEids.length; id++) {
                if (Integer.parseInt(PEids[id]) > total_CPUs) {
                    total_CPUs = Integer.parseInt(PEids[id]);
                }
            }

            //System.out.println("*** mach = "+MachCount);

            for (int ma = 0; ma < MachCount; ma++) {
                int totalPE = Integer.parseInt(PEs[ma]);

                PEList peList = new PEList();
                Double rel_speed = 1.0 + ((Double.parseDouble(values[2]) - cpu_speed.get(cpu_types.indexOf(values[4]))) / cpu_speed.get(cpu_types.indexOf(values[4])));

                String pokus = Long.toString(Math.round(rel_speed * 100000 * cpu_bench.get(cpu_types.indexOf(values[4]))));

                int peRating = Integer.parseInt(pokus);
                if (!ExperimentSetup.use_speeds) {
                    peRating = 1;
                }
                if (peRating > ExperimentSetup.maxPErating) {
                    ExperimentSetup.maxPErating = peRating;
                }
                //System.out.println(" PEs="+totalPE+" rating="+peRating);
                // 2. A Machine contains one or more PEs or CPUs. Therefore, should
                //    create an object of PEList to store these PEs before creating
                //    a Machine.

                // 3. Create PEs and add these into an object of PEList.
                for (int k = 0; k < totalPE; k++) {
                    // need to store PE id and MIPS Rating
                    peList.add(new PE(k, peRating));
                }
                // 4. Create one Machine with its id and list of PEs or CPUs
                mList.add(new MachineWithRAM(machine_id, peList,1000));
                machine_id++;
            }
            //System.out.println();
            //System.out.println(mList.size()+" = machines");


            // add this machine name to the list of machine names
            names.addLast(values[1]);
            last_machine = ((String) lines.get(j)).split("\t");



            // 5. Create a ResourceCharacteristics object that stores the
            //    properties of a Grid resource: architecture, OS, list of
            //    Machines (1 here), allocation policy: time- or space-shared, time zone
            //    and its price (G$/PE time unit).
            String arch = last_machine[4];      // system architecture e.g. Xeon, Opteron, Pentium3

            String os = last_machine[5];          // operating system e.g. Linux, Debian

            double time_zone = 0.0;         // time zone this resource located

            int ram = 0;
            if (last_machine[3].equals("unspecified")) {
                ram = 1000000;
            } else {
                ram = Integer.parseInt(last_machine[3]);
            }

            String properties = last_machine[6];


            // add this cluster name to the list of all clusters
            ExperimentSetup.clusterNames.addLast(name);
            // add the list of machines' names of this cluster to the list
            LinkedList mach_names = new LinkedList(names);
            ExperimentSetup.machineNames.addLast(mach_names);
            names.clear();
            ComplexResourceCharacteristics resConfig = new ComplexResourceCharacteristics(
                    arch, os, mList, ResourceCharacteristics.SPACE_SHARED, time_zone, cost, ram, properties, cpu_ids);

            // 6. Finally, we need to create a ComplexGridResource object.
            long seed = 11L * 13 * 17 * 19 * 23 + 1;
            double peakLoad = 0.0;       // the resource load during peak hour

            double offPeakLoad = 0.0;    // the resource load during off-peak hr

            double holidayLoad = 0.0;    // the resource load during holiday

            // incorporates weekends so the grid resource is on 7 days a week
            LinkedList Weekends = new LinkedList();
            Weekends.add(new Integer(Calendar.SATURDAY));
            Weekends.add(new Integer(Calendar.SUNDAY));

            // incorporates holidays. However, no holidays are set in this example
            LinkedList Holidays = new LinkedList();
            AllocPolicy apolicy = null;

            try {
                // this is usefull because we can define resources internal scheduling system (FCFS/RR/BackFilling,FairQueuing...)
                if (ExperimentSetup.use_RAM) {
                    AdvancedSpaceSharedWithRAM policy = null;
                    policy = new AdvancedSpaceSharedWithRAM(name, "AdvancedSpaceSharedPolicyWithRAM", resConfig);
                    ExperimentSetup.local_schedulers.add(policy);
                    apolicy = policy;

                } else {
                    AdvancedSpaceShared policy = null;
                    policy = new AdvancedSpaceShared(name, "AdvancedSpaceSharedPolicy", resConfig);
                    ExperimentSetup.local_schedulers.add(policy);
                    apolicy = policy;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                ResourceCalendar resCalendar = new ResourceCalendar(time_zone,
                        peakLoad, offPeakLoad, holidayLoad, Weekends,
                        Holidays, seed);

                ComplexGridResource gridRes = new ComplexGridResource(name, bandwidth, resConfig,
                        resCalendar, apolicy);
                int max = gridRes.getResourceCharacteristics().getNumPE();
                if (max > ExperimentSetup.maxPE) {
                    ExperimentSetup.maxPE = max;
                }


            } catch (Exception e) {
                System.out.println("Error in creating GridResource.");
                System.out.println(e.getMessage());
            }
            //resConfig.printCPUs();
            // reset values
            machine_id = 0;
            mList = new MachineList();
            cpu_ids = "";
            name = "Cluster_";


        }




    }

    /**
     * Creates Grid resources
     */
    protected void createPisaGridResources(double bandwidth, double cost, String data_set) {
        // read data-set from file
        LinkedList lines = new LinkedList();
        Input r = new Input();

        BufferedReader br = null;
        br = r.openFile(new File(ExperimentSetup.data_sets + "/" + data_set + ".machines"));
        r.getLines(lines, br);
        r.closeFile(br);
        // create machines from file
        for (int j = 0; j < lines.size(); j++) {
            //for (int j = 0; j < 150; j++) {
            String[] values = ((String) lines.get(j)).split(" ");
            int id = j;
            int totalPE = Integer.parseInt(values[0]);
            int peRating = Integer.parseInt(values[2]);
            String name = "r" + j;
            if (peRating > ExperimentSetup.maxPErating) {
                ExperimentSetup.maxPErating = peRating;
            }
            MachineList mList = new MachineList();


            // 2. A Machine contains one or more PEs or CPUs. Therefore, should
            //    create an object of PEList to store these PEs before creating
            //    a Machine.
            PEList peList = new PEList();


            // 3. Create PEs and add these into an object of PEList.
            for (int k = 0; k < totalPE; k++) {
                // need to store PE id and MIPS Rating
                peList.add(new PE(k, peRating));
            }

            // 4. Create one Machine with its id and list of PEs or CPUs
            mList.add(new Machine(0, peList));


            // 5. Create a ResourceCharacteristics object that stores the
            //    properties of a Grid resource: architecture, OS, list of
            //    Machines (1 here), allocation policy: time- or space-shared, time zone
            //    and its price (G$/PE time unit).
            String arch = "Sun Ultra";      // system architecture

            String os = "Solaris";          // operating system

            double time_zone = 0.0;         // time zone this resource located

            ComplexResourceCharacteristics resConfig = new ComplexResourceCharacteristics(
                    arch, os, mList, ResourceCharacteristics.SPACE_SHARED, time_zone, cost, 1024000, "", "");
            AllocPolicy apolicy = null;

            try {
                // this is usefull because we can define resources internal scheduling system (FCFS/RR/BackFilling,FairQueuing...)
                if (ExperimentSetup.use_RAM) {
                    AdvancedSpaceSharedWithRAM policy = null;
                    policy = new AdvancedSpaceSharedWithRAM(name, "AdvancedSpaceSharedPolicyWithRAM", resConfig);
                    ExperimentSetup.local_schedulers.add(policy);
                    apolicy = policy;

                } else {
                    AdvancedSpaceShared policy = null;
                    policy = new AdvancedSpaceShared(name, "AdvancedSpaceSharedPolicy", resConfig);
                    ExperimentSetup.local_schedulers.add(policy);
                    apolicy = policy;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // 6. Finally, we need to create a ComplexGridResource object.
            long seed = 11L * 13 * 17 * 19 * 23 + 1;
            double peakLoad = 0.0;       // the resource load during peak hour

            double offPeakLoad = 0.0;    // the resource load during off-peak hr

            double holidayLoad = 0.0;    // the resource load during holiday

            // incorporates weekends so the grid resource is on 7 days a week
            LinkedList Weekends = new LinkedList();
            Weekends.add(new Integer(Calendar.SATURDAY));
            Weekends.add(new Integer(Calendar.SUNDAY));

            // incorporates holidays. However, no holidays are set in this example
            LinkedList Holidays = new LinkedList();
            try {
                ResourceCalendar resCalendar = new ResourceCalendar(time_zone,
                        peakLoad, offPeakLoad, holidayLoad, Weekends,
                        Holidays, seed);

                ComplexGridResource gridRes = new ComplexGridResource(name, bandwidth, resConfig,
                        resCalendar, apolicy);
                //System.out.println(id+"\t"+resConfig.getResourceID());
            } catch (Exception e) {
                System.out.println("Error in creating GridResource.");
                System.out.println(e.getMessage());
            }
        }
    }
}
