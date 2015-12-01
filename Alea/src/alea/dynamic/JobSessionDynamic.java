/*
 Copyright (c) 2015 Simon Toth (kontakt@simontoth.cz)

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package alea.dynamic;

import xklusac.environment.ExperimentSetup;

/**
 * Dynamic User Session
 *
 * @author Simon Toth (kontakt@simontoth.cz)
 */
public class JobSessionDynamic {

    private final int sessionID;
    private final int firstArrival;
    private final int lastArrival;
    private final int lastCompletion;
    private double last_completed_batch;

    public int getSessionID() {
        return sessionID;
    }

    public int getFirstArrival() {
        return firstArrival;
    }

    public int getLastArrival() {
        return lastArrival;
    }

    public int getLastCompletion() {
        return lastCompletion;
    }

    public JobSessionDynamic(int session_id, int first_arrival, int last_arrival, int last_completion) {
        sessionID = session_id;
        firstArrival = first_arrival;
        lastArrival = last_arrival;
        lastCompletion = last_completion;
        last_completed_batch = -1;
    }

    public double getSimulationArrival(double simulation_time, DynamicModes simulation_mode) {
        if (simulation_mode == DynamicModes.DEPENDENCY_NATURAL_STATIC) {
            return Math.max(simulation_time,firstArrival - ExperimentSetup.firstArrival);
        }

        return simulation_time;
    }

    public void recordBatchCompletion(double simulation_time) {
        last_completed_batch = simulation_time;
    }

    public double adjustBatchArrival(double simulation_time, DynamicModes simulation_mode) {
        if (simulation_mode == DynamicModes.DEPENDENCY_NATURAL_STATIC) {
            if (last_completed_batch > 0) {
                return Math.max(simulation_time,last_completed_batch+(30*60)); // last completed batch + 30 minute think time
            } else {
                return Math.max(simulation_time,firstArrival - ExperimentSetup.firstArrival);
            }
        }

        return simulation_time;
    }
    
    public boolean isAcccepting(double simulation_time) {
        return simulation_time + ExperimentSetup.firstArrival + 35*60 < lastArrival;
    }

}
