/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xklusac.algorithms;

/**
 * Interface OptimizationAlgorithm<p>
 * This interface defines the method that is lately implemented by any schedule-based optimization algorithm.
 * @author       Dalibor Klusacek
 */

public interface OptimizationAlgorithm {

    public void execute(int rounds, int time_limit);

}
