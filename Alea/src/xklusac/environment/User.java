/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

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

    public User(String name) {
        this.setName(name);
        this.setJobs(0);
        this.setSlowdown(0.0);
        this.setResponse(0.0);
        this.setRuntime(0.0);
        this.setWait(0.0);
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
}
