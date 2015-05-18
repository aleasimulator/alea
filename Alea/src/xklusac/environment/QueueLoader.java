/*
 * MachineLoader.java
 *
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package xklusac.environment;


import java.io.BufferedReader;
import java.io.File;

import java.util.LinkedList;
import xklusac.extensions.Input;
import xklusac.extensions.Queue;

/**
 * Class MachineLoader<p> Creates GridResources according to specified data set.
 *
 * @author Dalibor Klusacek
 */
public class QueueLoader {   

    /**
     * Creates a new instance of MachineLoader
     */
    public QueueLoader(String data_set) {
        System.out.println("Starting Queue Loader ...");        
        init(data_set);
    }

    /**
     * Based on the type of workload, machines and Grid resources are generated
     * here.
     */
    private void init(String set) {
        createQueues(set);
    }

    /**
     * Creates Grid resources
     */
    protected void createQueues(String data_set) {
        // read data-set from file
        LinkedList lines = new LinkedList();
        Input r = new Input();
        String adresar = System.getProperty("user.dir");
        //System.out.println("Adresar = "+adresar);
        if (ExperimentSetup.data) {
            String[] path = adresar.split("/");
            if (path.length == 1) {
                path = adresar.split("\\\\");
            }
            adresar = "";
            for (int i = 0; i < path.length - 1; i++) {
                adresar += path[i] + "/";
            }
            //System.out.println("Adresar = "+adresar);
        }

        BufferedReader br = null;

        br = r.openFile(new File(adresar + "/data-set/" + data_set + ".queues"));
        System.out.println("Opening: " + adresar + "/data-set/" + data_set + ".queues");
        r.getLines(lines, br);
        r.closeFile(br);
        
        // create queues from file
        for (int j = 0; j < lines.size(); j++) {
            String[] values = ((String) lines.get(j)).split("\t");
            //System.out.println(lines.get(j));
            String name = values[0];
            int limit = Integer.parseInt(values[1]);
            int priority = Integer.parseInt(values[2]);        
            Queue q = new Queue(name, limit, priority);
            ExperimentSetup.queues.put(name, q);
            Scheduler.all_queues.addLast(new LinkedList<GridletInfo>());
            Scheduler.all_queues_names.addLast(name);
    }

    }
}
