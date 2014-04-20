/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.extensions;

/**
 *
 * @author dalibor
 */
public class Queue {
    private String name;
    private int limit;
    private int priority;
    private int used;
    
    public Queue(String name, int limit, int priority){
        this.name = name;
        this.limit = limit;
        this.priority = priority;
        this.used = 0;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the used
     */
    public int getUsed() {
        return used;
    }

    /**
     * @param used the used to set
     */
    public void setUsed(int running) {
        this.used = running;
    }
    
    public int getAvailCPUs() {
        return (limit - used);
    }
    
}
