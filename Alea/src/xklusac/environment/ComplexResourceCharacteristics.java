/*
 * ComplexResourceCharacteristics.java
 *
 * Created on 3. prosinec 2008, 11:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package xklusac.environment;

import gridsim.MachineList;
import gridsim.ResourceCharacteristics;
import java.util.LinkedList;

/**
 * Class ComplexResourceCharacteristics<p>
 * extends Resource description.<p>
 * In the future it will allow the use of RAM, HDD, etc. parameters.
 *
 * @author Dalibor Klusacek
 */
public class ComplexResourceCharacteristics extends ResourceCharacteristics {

    /**
     * RAM size in KB
     */
    private long ramOnOneMachine;
    /**
     * additional properties of this resource
     */
    private String properties;
    /**
     * not used in Alea 2.0
     */
    private String cpu_ids_string;
    /**
     * not used in Alea 2.0
     */
    private LinkedList<Integer> cpu_ids = new LinkedList();
    /**
     * not used in Alea 2.0
     */
    private boolean failed;

    /**
     * Creates a new instance of ComplexResourceCharacteristics
     */
    public ComplexResourceCharacteristics(String architecture, String OS,
            MachineList machineList, int allocationPolicy, double timeZone,
            double costPerSec, long ram, String properties, String cpu_ids) {
        super(architecture, OS, machineList, allocationPolicy, timeZone, costPerSec);
        this.setRamOnOneMachine(ram);
        this.setProperties(properties);
        this.setFailed(false);
        //this.setCpu_ids_string(cpu_ids);
    }

    public long getRamOnOneMachine() {
        return ramOnOneMachine;
    }

    public void setRamOnOneMachine(long ramOnOneMachine) {
        this.ramOnOneMachine = ramOnOneMachine;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public LinkedList getCpu_ids() {
        return cpu_ids;
    }

    public void setCpu_ids_string(String cpu_ids_string) {
        this.cpu_ids_string = cpu_ids_string;

        String values[] = cpu_ids_string.split(",");
        for (int i = 0; i < values.length; i++) {
            this.cpu_ids.add(Integer.parseInt(values[i]));
        }
    }

    public void printCPUs() {
        for (int i = 0; i < cpu_ids.size(); i++) {
            System.out.print(cpu_ids.get(i) + ",");
        }
        System.out.println();
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

}
