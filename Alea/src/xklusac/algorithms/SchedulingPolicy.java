/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xklusac.algorithms;

import xklusac.environment.GridletInfo;

/**
 * Interface SchedulingPolicy<p>
 * This class is an interface to any scheduling policy.
 * @author       Dalibor Klusacek
 */

public interface SchedulingPolicy {

    /** Adds new job into queue/schedule according to applied strategy */
    public void addNewJob(GridletInfo gi);

    /** Selects job for execution using applied strategy */
    public int selectJob();
   
}
