/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

import java.util.ArrayList;

/**
 *
 * @author Dalibor
 */
public class User {

    private String name;
    private int jobs;
    private double slowdown;
    private double response;
    private double wait;
    private double runtime;
    private ArrayList<Double> percentages;

    public User(String name) {
        this.setName(name);
        this.setJobs(0);
        this.setSlowdown(0.0);
        this.setResponse(0.0);
        this.setRuntime(0.0);
        this.setWait(0.0);
        this.percentages = new ArrayList<Double>();
    }

    public int getJobs() {
        return jobs;
    }

    public void setJobs(int jobs) {
        this.jobs = jobs;
    }

    public double getSlowdown() {
        return slowdown;
    }

    public void setSlowdown(double slowdown) {
        this.slowdown = slowdown;
    }

    public double getResponse() {
        return response;
    }

    public void setResponse(double response) {
        this.response = response;
    }

    public double getWait() {
        return wait;
    }

    public void setWait(double wait) {
        this.wait = wait;
    }

    public double getRuntime() {
        return runtime;
    }

    public void setRuntime(double runtime) {
        this.runtime = runtime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateRuntime(double runtime) {
        this.runtime += runtime;
    }

    public void updateJobs(double jobs) {
        this.jobs += jobs;
    }

    public void updateSlowdown(double slowdown) {
        this.slowdown += slowdown;
    }

    public void updateWait(double wait) {
        this.wait += wait;
    }

    public void updateResponse(double response) {
        this.response += response;
    }

    /**
     * @return the percentages
     */
    public ArrayList getPercentages() {
        return percentages;
    }

    /**
     * @param percentages the percentages to set
     */
    public void setPercentages(ArrayList percentages) {
        this.percentages = percentages;
    }

    public void addPercentage(double perc) {
        percentages.add(perc);
        if (percentages.size() > 5) {
            percentages.remove(0);
        }
    }

    public double getAvgPercentage() {
        double avg = 0.0;
        for (int i = 0; i < percentages.size(); i++) {
            avg += percentages.get(i);
        }
        if (percentages.size() > 0) {
            return avg / percentages.size();
        } else {
            return 1.0;
        }
    }

    public double getMinPercentage() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < percentages.size(); i++) {
            if (min > percentages.get(i)) {
                min = percentages.get(i);
            }
        }
        if (percentages.size() > 0) {
            return min;
        } else {
            return 1.0;
        }
    }

    public String printPercentage() {
        String p = "";
        for (int i = 0; i < percentages.size(); i++) {
            p += Math.round(percentages.get(i) * 10.0) / 10.0 + ", ";
        }
        return p;
    }
}
