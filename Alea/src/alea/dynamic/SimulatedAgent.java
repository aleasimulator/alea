package alea.dynamic;

import eduni.simjava.Sim_event;
import xklusac.environment.ComplexGridlet;

public interface SimulatedAgent {

    /**
     * Process GridSim Event
     *
     * @param ev Received event
     * @param time Simulation timestamp
     */
    public void process_event(Sim_event ev, double time);

    /**
     * Process a manual wake-up
     *
     * @return AleaSimTag
     */
    public int process_wake_up(double time);

    /**
     * Read a gridlet from this agent.
     *
     * The returned gridlet is prepared in process_wake_up().
     *
     * @return Pre-prepared gridlet
     */
    public ComplexGridlet get_current_gridlet();

    /**
     * Get the number of processed jobs
     *
     * @return Number of processed jobs
     */
    public int get_total_processed_jobs();

    public double get_next_wakeup();

}
