package xklusac.environment;

import alea.core.AleaSimTags;
import gridsim.*;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_system;
import java.util.LinkedList;
import xklusac.environment.FailureLoaderNew.Failure;

/**
 * Class AdvancedSpaceShared (version 1.0)<p>
 * This class is an allocation policy for GridResource that behaves
 * exactly like First Come First Serve (FCFS). This is a basic and simple
 * scheduler that runs each Gridlet to one or more Processing Element (PE).
 * If a Gridlet requires more than one PE, then this scheduler wait till these PEs are
 * available. If the gridlet requires more PEs than is available in the whole resource the gridlet is returned
 * as FAILED. In this version <b>simple co-allocation</b> is allowed such that parallel jobs may be executed on one or more machines
 * if necessary (not enough PEs is available on single machine).<p>
 * Moreover, it allows to simulate specific machine's failures, killing (and returning to the Scheduler) such jobs at such machine.
 *
 * @author Manzur Murshed and Rajkumar Buyya
 * @author Anthony Sulistio (re-written this class)
 * @author Dalibor Klusacek (re-named and extended this class)
 * @since GridSim Toolkit 2.2
 */
class AdvancedSpaceShared extends AllocPolicy {

    private ResGridletList gridletQueueList_;     // Queue list
    private ResGridletList gridletInExecList_;    // Execution list
    private ResGridletList gridletPausedList_;    // Pause list
    private double lastUpdateTime_;    // the last time Gridlets updated
    private int[] machineRating_;      // list of machine ratings available
    double used_mips = 0.0;
    double used_usage = 0.0;
    boolean failed = false;
    boolean mach_failed = false;
    double failure_time = 0.0;
    double wfailure_time = 0.0;
    ComplexResourceCharacteristics resource_ = null;
    double last_time = 0.0;
    int killed_cpus = 0;
    int shortened = 0;
    String name;

    /**
     * Allocates a new AdvancedSpaceShared object
     *
     * @param resourceName    the GridResource entity name that will contain
     *                        this allocation policy
     * @param entityName      this object entity name
     * @see gridsim.GridSim#init(int, Calendar, boolean, String[], String[],
     *          String)
     * @post $none
     * @pre resourceName != null
     * @pre entityName != null
     * @throws Exception This happens when one of the following scenarios occur:
     *      <ul>
     *          <li> creating this entity before initializing GridSim package
     *          <li> this entity name is <tt>null</tt> or empty
     *          <li> this entity has <tt>zero</tt> number of PEs (Processing
     *              Elements). <br>
     *              No PEs mean the Gridlets can't be processed.
     *              A GridResource must contain one or more Machines.
     *              A Machine must contain one or more PEs.
     *      </ul>
     */
    AdvancedSpaceShared(String resourceName, String entityName, ComplexResourceCharacteristics resConfig) throws Exception {
        super(resourceName, entityName);
        this.name = entityName;
        // initialises local data structure
        this.gridletInExecList_ = new ResGridletList();
        this.gridletPausedList_ = new ResGridletList();
        this.gridletQueueList_ = new ResGridletList();
        this.lastUpdateTime_ = 0.0;
        this.machineRating_ = null;
        this.failed = false;
        this.resource_ = resConfig;
        this.failure_time = 0.0;
        this.wfailure_time = 0.0;
        this.killed_cpus = 0;
    }

    /**
     * Handles internal events that are coming to this entity.
     * @pre $none
     * @post $none
     */
    public void body() {
        // Gets the PE's rating for each Machine in the list.
        // Assumed one Machine has same PE rating.
        MachineList list = super.resource_.getMachineList();
        int size = list.size();
        int indexPE = 0;
        machineRating_ = new int[size];
        for (int i = 0; i < size; i++) {
            PEList pl = list.getMachine(i).getPEList();
            // in the start time there is for sure free PE - allows us give different IDs to PEs
            indexPE = pl.getFreePEID();
            machineRating_[i] = super.resource_.getMIPSRatingOfOnePE(i, indexPE);
        }


        // a loop that is looking for internal events only
        Sim_event ev = new Sim_event();
        while (Sim_system.running()) {
            super.sim_get_next(ev);

            // if the simulation finishes then exit the loop
            if (ev.get_tag() == GridSimTags.END_OF_SIMULATION ||
                    super.isEndSimulation() == true) {
                double mips = resource_.getMIPSRating() * GridSim.clock();
                mips -= wfailure_time;
                double usage = resource_.getNumPE() * GridSim.clock();
                usage -= failure_time;

                used_mips = Math.round((used_mips / mips) * 10000.0);
                used_usage = Math.round((used_usage / usage) * 10000.0);
                System.out.println(resource_.getResourceName() + " wusage = " + used_mips / 100.0 + "%, usage = " + used_usage / 100.0 + "%, shortened = " + shortened);
                failure_time = 0.0;
                wfailure_time = 0.0;
                used_mips = 0.0;
                used_usage = 0.0;
                break;
            }

            // failure finished - restart this resource
            if (ev.get_tag() == AleaSimTags.FAILURE_FINISHED) {
                failed = false;
                list = super.resource_.getMachineList();
                size = list.size();
                for (int i = 0; i < size; i++) {
                    list.getMachine(i).setFailed(false);
                }
                // update machine usage
                Scheduler.load += (Scheduler.activePEs / Scheduler.availPEs) * (GridSim.clock() - Scheduler.last_event);
                Scheduler.classic_load += (Scheduler.classic_activePEs / Scheduler.classic_availPEs) * (GridSim.clock() - Scheduler.last_event);
                Scheduler.max_load += 1.0 * (GridSim.clock() - Scheduler.last_event);
                Scheduler.last_event = GridSim.clock();

                // update number of CPUs
                Scheduler.classic_availPEs += this.totalPE_;
                Scheduler.availPEs += this.totalPE_ * this.resource_.getMIPSRatingOfOnePE();
                //System.out.println(resName_ + ": restart all="+this.resource_.isWorking());
                super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.FAILURE_RESTART, this.resId_);
                continue;
            }

            if (ev.get_tag() == AleaSimTags.FAILURE_RESTART2) {
                Failure failure = (Failure) ev.get_data();
                mach_failed = false;
                int[] ids = failure.getIds();
                list = super.resource_.getMachineList();

                int onPEs = 0;
                double onMIPS = 0.0;

                for (int i = 0; i < ids.length; i++) {
                    list.getMachine(ids[i]).setFailed(false);
                    //System.out.println(resName_ + " starting machine: "+ids[i]);
                    onPEs += list.getMachine(ids[i]).getNumPE();
                    onMIPS += list.getMachine(ids[i]).getNumPE() * this.resource_.getMIPSRatingOfOnePE();

                }
                // update machine usage
                Scheduler.load += (Scheduler.activePEs / Scheduler.availPEs) * (GridSim.clock() - Scheduler.last_event);
                Scheduler.classic_load += (Scheduler.classic_activePEs / Scheduler.classic_availPEs) * (GridSim.clock() - Scheduler.last_event);
                Scheduler.max_load += 1.0 * (GridSim.clock() - Scheduler.last_event);
                Scheduler.last_event = GridSim.clock();

                // update number of CPUs
                Scheduler.classic_availPEs += onPEs;
                Scheduler.availPEs += onMIPS;
                allocateQueueGridlet();
                //System.out.println(resName_ + ": restart some, all on?="+this.resource_.isWorking());
                //System.out.println(Math.round(GridSim.clock())+": restart of: "+resName_ +" restart: "+ids.length+" machines. ["+ids[0]+"](x2), running: "+getNumRunning()+"/"+this.resource_.getNumPE()+",  from: "+Math.round(failure.getTime()));
                super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.FAILURE_RESTART, this.resId_);
                continue;
            }
            if (ev.get_tag() == AleaSimTags.FAILURE_START) {
                super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.FAILURE_START, this.resId_+"x"+this.killed_cpus);
                //super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.FAILURE_START, this.killed_cpus);
                continue;
            }


            // Internal Event if the event source is this entity
            if (ev.get_src() == super.myId_ && gridletInExecList_.size() > 0) {

                updateGridletProcessing();   // update Gridlets
                checkGridletCompletion();    // check for finished Gridlets
            }
        }

        // CHECK for ANY INTERNAL EVENTS WAITING TO BE PROCESSED
        while (super.sim_waiting() > 0) {
            // wait for event and ignore since it is likely to be related to
            // internal event scheduled to update Gridlets processing
            super.sim_get_next(ev);

            if (ev.get_tag() == AleaSimTags.FAILURE_FINISHED || ev.get_tag() == AleaSimTags.FAILURE_RESTART2) {
                System.out.println(super.resName_ + " ignore resource/machine restart.");
            } else {
                System.out.println(super.resName_ +
                        ".SpaceShared.body(): ignore internal events");
            }
        }
    }

    /**
     * Schedules a new Gridlet that has been received by the GridResource
     * entity. If the gridlet requires more than 1 PE it waits until all
     * required PEs are free. If the resource does not have enough PEs to
     * run the gridlet, gridlet is returned as FAILED.
     * @param   gl    a Gridlet object that is going to be executed
     * @param   ack   an acknowledgement, i.e. <tt>true</tt> if wanted to know
     *        whether this operation is success or not, <tt>false</tt>
     *        otherwise (don't care)
     * @pre gl != null
     * @post $none
     */
    public void gridletSubmit(Gridlet gl, boolean ack) {
        // update the current Gridlets in exec list up to this point in time
        updateGridletProcessing();
        boolean success = false;
        boolean failure = false;

        //System.out.println(gl.getGridletID()+" processing...");
        long time_limit = ((ComplexGridlet) gl).getJobLimit();
        double expected_runtime = gl.getGridletLength() / this.resource_.getMIPSRatingOfOnePE();
        // removed time_limit + 300
        if (expected_runtime > (time_limit)) {
            // we have to shorten the job so that the time limit is not exceeded
            //System.out.println(gl.getGridletID()+" exceeds time limit by :"+ Math.round(expected_runtime - time_limit)/60.0+" minutes, limit = "+(time_limit)+" sec.");
            double new_length = (time_limit) * this.resource_.getMIPSRatingOfOnePE();
            gl.setGridletLength(new_length);
            //System.out.println(gl.getGridletID()+" new length = "+Math.round(new_length));
            shortened++;
        }
        ResGridlet rgl = new ResGridlet(gl);

        int numPE = gl.getNumPE();

        // require more than 1 CPU
        if (gl.getNumPE() > 1) {
            //String userName = GridSim.getEntityName( gl.getUserID() );
            int freePE = this.getNumFreePE();
            // Do not allow anticipating if there is a gridlet in waiting queue. This can happen
            // when i.e., single PE requiring gridlet arrive and only 1 slot is free at the moment.
            if (numPE <= freePE && gridletQueueList_.size() == 0) {
                // Start now - Enough free PEs on resource
                success = allocatePEtoGridlet(rgl, numPE);
            } else {
                if (gl.getNumPE() > super.totalPE_) {
                    // Resource has low number of PEs to run this gridlet...
                    System.out.println("Gridlet FAILED: not enough CPUs, requested=" + gl.getNumPE() + ", max. available=" + super.totalPE_);
                    rgl.setGridletStatus(Gridlet.FAILED);
                    rgl.finalizeGridlet();
                    super.sendFinishGridlet(rgl.getGridlet());
                    failure = true;
                }
            }
        // require only 1 CPU
        } else {
            // if there is an available PE slot, then allocate immediately

            if (this.getNumFreePE() > 0 && gridletQueueList_.size() == 0) {

                success = allocatePEtoGridlet(rgl);
            //System.out.println(rgl.getGridletID()+" is alloc = "+success);
            }
        }
        // if no available PE then put the ResGridlet into a Queue list
        if (success == false && failure == false) {
            rgl.setGridletStatus(Gridlet.QUEUED);
            System.out.println(rgl.getGridletID() + " = Gridlet QUEUED because free:" + this.getNumFreePE() + " while reqested:" + rgl.getNumPE() + " on: " + this.resource_.getResourceName() + " Potential problem +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            gridletQueueList_.add(rgl);
        }
        
        //System.out.println(rgl.getGridletID()+" >>> "+rgl.getGridlet().getGridletStatusString());
        // sends back an ack if required
        if (ack == true && failure == false) {
            super.sendAck(GridSimTags.GRIDLET_SUBMIT_ACK, true,
                    gl.getGridletID(), gl.getUserID());
        }
        
        // allow next scheduling run
        if (failure == false && success == true) {
            sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.GRIDLET_STARTED, gl);
        }
    }

    /**
     * Finds the status of a specified Gridlet ID.
     * @param gridletId    a Gridlet ID
     * @param userId       the user or owner's ID of this Gridlet
     * @return the Gridlet status or <tt>-1</tt> if not found
     * @see gridsim.Gridlet
     * @pre gridletId > 0
     * @pre userId > 0
     * @post $none
     */
    public int gridletStatus(int gridletId, int userId) {
        ResGridlet rgl = null;

        // Find in EXEC List first
        int found = super.findGridlet(gridletInExecList_, gridletId, userId);
        if (found >= 0) {
            // Get the Gridlet from the execution list
            rgl = (ResGridlet) gridletInExecList_.get(found);
            return rgl.getGridletStatus();
        }

        // Find in Paused List
        found = super.findGridlet(gridletPausedList_, gridletId, userId);
        if (found >= 0) {
            // Get the Gridlet from the execution list
            rgl = (ResGridlet) gridletPausedList_.get(found);
            return rgl.getGridletStatus();
        }

        // Find in Queue List
        found = super.findGridlet(gridletQueueList_, gridletId, userId);
        if (found >= 0) {
            // Get the Gridlet from the execution list
            rgl = (ResGridlet) gridletQueueList_.get(found);
            return rgl.getGridletStatus();
        }

        // if not found in all 3 lists then no found
        return -1;
    }

    /**
     * Cancels a Gridlet running in this entity.
     * This method will search the execution, queued and paused list.
     * The User ID is
     * important as many users might have the same Gridlet ID in the lists.
     * <b>NOTE:</b>
     * <ul>
     *    <li> Before canceling a Gridlet, this method updates all the
     *         Gridlets in the execution list. If the Gridlet has no more MIs
     *         to be executed, then it is considered to be <tt>finished</tt>.
     *         Hence, the Gridlet can't be canceled.
     *
     *    <li> Once a Gridlet has been canceled, it can't be resumed to
     *         execute again since this method will pass the Gridlet back to
     *         sender, i.e. the <tt>userId</tt>.
     *
     *    <li> If a Gridlet can't be found in both execution and paused list,
     *         then a <tt>null</tt> Gridlet will be send back to sender,
     *         i.e. the <tt>userId</tt>.
     * </ul>
     *
     * @param gridletId    a Gridlet ID
     * @param userId       the user or owner's ID of this Gridlet
     * @pre gridletId > 0
     * @pre userId > 0
     * @post $none
     */
    public void gridletCancel(int gridletId, int userId) {
        // cancels a Gridlet
        ResGridlet rgl = cancel(gridletId, userId);

        // if the Gridlet is not found
        if (rgl == null) {
            System.out.println(super.resName_ +
                    ".SpaceShared.gridletCancel(): Cannot find " +
                    "Gridlet #" + gridletId + " for User #" + userId);

            super.sendCancelGridlet(GridSimTags.GRIDLET_CANCEL, null,
                    gridletId, userId);
            return;
        }

        // if the Gridlet has finished beforehand then prints an error msg
        if (rgl.getGridletStatus() == Gridlet.SUCCESS) {
            System.out.println(super.resName_ + ".SpaceShared.gridletCancel(): Cannot cancel" + " Gridlet #" + gridletId + " for User #" + userId + " since it has FINISHED.");
        }

        // sends the Gridlet back to sender
        rgl.finalizeGridlet();
        super.sendCancelGridlet(GridSimTags.GRIDLET_CANCEL, rgl.getGridlet(),
                gridletId, userId);
    }

    /**
     * Pauses a Gridlet only if it is currently executing.
     * This method will search in the execution list. The User ID is
     * important as many users might have the same Gridlet ID in the lists.
     * @param gridletId    a Gridlet ID
     * @param userId       the user or owner's ID of this Gridlet
     * @param   ack   an acknowledgement, i.e. <tt>true</tt> if wanted to know
     *        whether this operation is success or not, <tt>false</tt>
     *        otherwise (don't care)
     * @pre gridletId > 0
     * @pre userId > 0
     * @post $none
     */
    public void gridletPause(int gridletId, int userId, boolean ack) {
        boolean status = false;

        // Find in EXEC List first
        int found = super.findGridlet(gridletInExecList_, gridletId, userId);
        if (found >= 0) {
            // updates all the Gridlets first before pausing
            updateGridletProcessing();

            // Removes the Gridlet from the execution list
            ResGridlet rgl = (ResGridlet) gridletInExecList_.remove(found);

            // if a Gridlet is finished upon cancelling, then set it to success
            // instead.
            if (rgl.getRemainingGridletLength() == 0.0) {
                found = -1;  // meaning not found in Queue List
                gridletFinish(rgl, Gridlet.SUCCESS);
                System.out.println(super.resName_ + ".SpaceShared.gridletPause(): Cannot pause" + " Gridlet #" + gridletId + " for User #" + userId + " since it has FINISHED.");
            } else {
                status = true;
                rgl.setGridletStatus(Gridlet.PAUSED);  // change the status
                gridletPausedList_.add(rgl);   // add into the paused list

                // Set the PE on which Gridlet finished to FREE
                super.resource_.setStatusPE(PE.FREE, rgl.getMachineID(),
                        rgl.getPEID());

                // empty slot is available, hence process a new Gridlet
                allocateQueueGridlet();
            }
        } else {      // Find in QUEUE list
            found = super.findGridlet(gridletQueueList_, gridletId, userId);
        }

        // if found in the Queue List
        if (status == false && found >= 0) {
            status = true;

            // removes the Gridlet from the Queue list
            ResGridlet rgl = (ResGridlet) gridletQueueList_.remove(found);
            rgl.setGridletStatus(Gridlet.PAUSED);   // change the status
            gridletPausedList_.add(rgl);            // add into the paused list
        } // if not found anywhere in both exec and paused lists
        else if (found == -1) {
            System.out.println(super.resName_ +
                    ".SpaceShared.gridletPause(): Error - cannot " +
                    "find Gridlet #" + gridletId + " for User #" + userId);
        }
        
        System.out.println(super.resName_ + ".SpaceShared.gridletPause(): Gridlet was paused!!! ");

        // sends back an ack if required
        if (ack == true) {
            super.sendAck(GridSimTags.GRIDLET_PAUSE_ACK, status,
                    gridletId, userId);
        }
    }

    /**
     * Moves a Gridlet from this GridResource entity to a different one.
     * This method will search in both the execution and paused list.
     * The User ID is important as many Users might have the same Gridlet ID
     * in the lists.
     * <p>
     * If a Gridlet has finished beforehand, then this method will send back
     * the Gridlet to sender, i.e. the <tt>userId</tt> and sets the
     * acknowledgment to false (if required).
     *
     * @param gridletId    a Gridlet ID
     * @param userId       the user or owner's ID of this Gridlet
     * @param destId       a new destination GridResource ID for this Gridlet
     * @param   ack   an acknowledgement, i.e. <tt>true</tt> if wanted to know
     *        whether this operation is success or not, <tt>false</tt>
     *        otherwise (don't care)
     * @pre gridletId > 0
     * @pre userId > 0
     * @pre destId > 0
     * @post $none
     */
    public void gridletMove(int gridletId, int userId, int destId, boolean ack) {
        // cancels the Gridlet
        ResGridlet rgl = cancel(gridletId, userId);

        // if the Gridlet is not found
        if (rgl == null) {
            System.out.println(super.resName_ +
                    ".SpaceShared.gridletMove(): Cannot find " +
                    "Gridlet #" + gridletId + " for User #" + userId);

            if (ack == true) // sends back an ack if required
            {
                super.sendAck(GridSimTags.GRIDLET_SUBMIT_ACK, false,
                        gridletId, userId);
            }

            return;
        }

        // if the Gridlet has finished beforehand
        if (rgl.getGridletStatus() == Gridlet.SUCCESS) {
            System.out.println(super.resName_ + ".SpaceShared.gridletMove(): Cannot move Gridlet #" + gridletId + " for User #" + userId + " since it has FINISHED.");

            if (ack == true) // sends back an ack if required
            {
                super.sendAck(GridSimTags.GRIDLET_SUBMIT_ACK, false,
                        gridletId, userId);
            }

            gridletFinish(rgl, Gridlet.SUCCESS);
        } else // otherwise moves this Gridlet to a different GridResource
        {
            rgl.finalizeGridlet();

            // Set PE on which Gridlet finished to FREE
            super.resource_.setStatusPE(PE.FREE, rgl.getMachineID(),
                    rgl.getPEID());

            super.gridletMigrate(rgl.getGridlet(), destId, ack);
            allocateQueueGridlet();
        }
    }

    /**
     * Resumes a Gridlet only in the paused list.
     * The User ID is important as many Users might have the same Gridlet ID
     * in the lists.
     * @param gridletId    a Gridlet ID
     * @param userId       the user or owner's ID of this Gridlet
     * @param   ack   an acknowledgement, i.e. <tt>true</tt> if wanted to know
     *        whether this operation is success or not, <tt>false</tt>
     *        otherwise (don't care)
     * @pre gridletId > 0
     * @pre userId > 0
     * @post $none
     */
    public void gridletResume(int gridletId, int userId, boolean ack) {
        boolean status = false;

        // finds the Gridlet in the execution list first
        int found = super.findGridlet(gridletPausedList_, gridletId, userId);
        if (found >= 0) {
            // removes the Gridlet
            ResGridlet rgl = (ResGridlet) gridletPausedList_.remove(found);
            rgl.setGridletStatus(Gridlet.RESUMED);

            // update the Gridlets up to this point in time
            updateGridletProcessing();
            status = true;

            // if there is an available PE slot, then allocate immediately
            boolean success = false;
            if (gridletInExecList_.size() < super.totalPE_) {
                success = allocatePEtoGridlet(rgl);
            }

            // otherwise put into Queue list
            if (success == false) {
                rgl.setGridletStatus(Gridlet.QUEUED);
                gridletQueueList_.add(rgl);
            }

            System.out.println(super.resName_ + "TimeShared.gridletResume():" +
                    " Gridlet #" + gridletId + " with User ID #" +
                    userId + " has been sucessfully RESUMED.");
        } else {
            System.out.println(super.resName_ +
                    "TimeShared.gridletResume(): Cannot find " +
                    "Gridlet #" + gridletId + " for User #" + userId);
        }

        // sends back an ack if required
        if (ack == true) {
            super.sendAck(GridSimTags.GRIDLET_RESUME_ACK, status,
                    gridletId, userId);
        }
    }
    ///////////////////////////// PRIVATE METHODS /////////////////////
    /**
     * Allocates the first Gridlet in the Queue list (if any) to execution list
     * If there are more gridlets that can be allocated then allocate them. This
     * is usually possible when previously finished gridlet required more PEs (CPUs)
     * while queued gridlets require less.
     * @pre $none
     * @post $none
     */
    private void allocateQueueGridlet() {
        // if there are many Gridlets in the QUEUE, then allocate a
        // PE to the first Gridlet in the list since it follows FCFS
        // (First Come First Serve) approach. Then removes the Gridlet from
        // the Queue list
        boolean success = true;
        // repeat till it is possible to allocate queued gridlets
        while (success) {
            success = false;

            //if (gridletQueueList_.size() > 0 && resource_.getNumBusyPE() < super.totalPE_ ) {
            if (gridletQueueList_.size() > 0 && this.getNumFreePE() > 0) {
                ResGridlet obj = (ResGridlet) gridletQueueList_.getFirst();

                // allocate the Gridlet into an empty PE slot and remove it from
                // the queue list

                if (obj.getNumPE() > 1) {
                    if (resource_.getNumFreePE() >= obj.getNumPE()) {
                        // Do not even try to allocate if not enough PEs free
                        success = allocatePEtoGridlet(obj, obj.getNumPE());
                    }
                } else {
                    success = allocatePEtoGridlet(obj);
                }
                if (success == true) {
                    // Remove only when successfully done
                    gridletQueueList_.remove(obj);
                }
            }
        }
    }

    /**
     * Updates the execution of all Gridlets for a period of time.
     * The time period is determined from the last update time up to the
     * current time. Once this operation is successfull, then the last update
     * time refers to the current time.
     * @pre $none
     * @post $none
     */
    private void updateGridletProcessing() {
        // Identify MI share for the duration (from last event time)
        double time = GridSim.clock();
        double timeSpan = time - lastUpdateTime_;

        // if current time is the same or less than the last update time,
        // then ignore
        if (timeSpan <= 0.0) {
            return;
        }

        // Update Current Time as Last Update
        lastUpdateTime_ = time;

        // update the GridResource load
        int size = gridletInExecList_.size();
        double load = super.calculateTotalLoad(size);
        super.addTotalLoad(load);

        // if no Gridlets in execution then ignore the rest
        if (size == 0) {
            return;
        }

        ResGridlet obj = null;

        // a loop that allocates MI share for each Gridlet accordingly
        /*Iterator iter = gridletInExecList_.iterator();
        while ( iter.hasNext() ) {
        obj = (ResGridlet) iter.next();
        
        // Updates the Gridlet length that is currently being executed
        load = getMIShare( timeSpan, obj.getMachineID());
        obj.updateGridletFinishedSoFar(load);
        }*/
        for (int r = 0; r < gridletInExecList_.size(); r++) {
            obj = (ResGridlet) gridletInExecList_.get(r);

            // Updates the Gridlet length that is currently being executed
            load = getMIShare(timeSpan, obj.getMachineID());
            obj.updateGridletFinishedSoFar(load);

        }

    }

    /**
     * Identifies MI share (max and min) each Gridlet gets for
     * a given timeSpan
     * @param timeSpan     duration
     * @param machineId    machine ID that executes this Gridlet
     * @return  the total MI share that a Gridlet gets for a given
     *          <tt>timeSpan</tt>
     * @pre timeSpan >= 0.0
     * @pre machineId > 0
     * @post $result >= 0.0
     */
    private double getMIShare(double timeSpan, int machineId) {
        // 1 - localLoad_ = available MI share percentage
        //double localLoad = super.resCalendar_.getCurrentLoad();
        double localLoad = 0.0;
        //System.out.println("LocalLoad =========== "+localLoad);
        // each Machine might have different PE Rating compare to another
        // so much look at which Machine this PE belongs to
        double totalMI = machineRating_[machineId] * timeSpan * (1 - localLoad);
        return totalMI;
    }

    /**
     * Allocates a Gridlet into a free PE and sets the Gridlet status into
     * INEXEC and PE status into busy afterwards
     * @param rgl  a ResGridlet object
     * @return <tt>true</tt> if there is an empty PE to process this Gridlet,
     *         <tt>false</tt> otherwise
     * @pre rgl != null
     * @post $none
     */
    private boolean allocatePEtoGridlet(ResGridlet rgl) {
        // IDENTIFY MACHINE which has a free PE and add this Gridlet to it.
        MachineWithRAM myMachine = null;
        MachineList mList = resource_.getMachineList();
        //System.out.println(rgl.getGridletID()+" start..."+mList.size());
        int peIndex = -1;
        // gets the list of PEs and find one empty PE

        //System.out.println(rgl.getGridletID()+" avail machines = ");
        for (int i = 0; i < mList.size(); i++) {
            myMachine = (MachineWithRAM) mList.get(i);
            if (!myMachine.getFailed() && myMachine.getNumFreePE() > 0) {
                break;
            } else {
                peIndex += myMachine.getNumPE();
                myMachine = null;
            }

        }
        // If a Machine is empty then ignore the rest
        if (myMachine == null) {
            System.out.println(rgl.getGridletID() + " null machine");
            return false;
        }
        //System.out.println(rgl.getGridletID()+" not null machine...");
        PEList MyPEList = myMachine.getPEList();
        int freePE = MyPEList.getFreePEID();
        peIndex += freePE + 1;
        // ALLOCATE IMMEDIATELY

        rgl.setMachineAndPEID(myMachine.getMachineID(), freePE);

        // set PEs list
        LinkedList<Integer> PEs = new LinkedList();
        PEs.add(peIndex);
        ((ComplexGridlet) rgl.getGridlet()).setPEs(PEs);
        rgl.setGridletStatus(Gridlet.INEXEC);   // change Gridlet status
        // add this Gridlet into execution list
        gridletInExecList_.add(rgl);
        // Set allocated PE to BUSY status
        super.resource_.setStatusPE(PE.BUSY, rgl.getMachineID(), freePE);

        // Identify Completion Time and Set Interrupt
        int rating = machineRating_[rgl.getMachineID()];
        double time = forecastFinishTime(rating,
                rgl.getRemainingGridletLength());

        int roundUpTime = (int) (time + 1);   // rounding up
        rgl.setFinishTime(roundUpTime);


        //update machine usage
        Scheduler.load += (Scheduler.activePEs / Scheduler.availPEs) * (GridSim.clock() - Scheduler.last_event);
        Scheduler.classic_load += (Scheduler.classic_activePEs / Scheduler.classic_availPEs) * (GridSim.clock() - Scheduler.last_event);
        Scheduler.max_load += 1.0 * (GridSim.clock() - Scheduler.last_event);
        Scheduler.last_event = GridSim.clock();
        Scheduler.activePEs += 1 * super.resource_.getMIPSRatingOfOnePE();
        Scheduler.classic_activePEs += 1;

        // then send this into itself
        super.sim_schedule(super.myId_, roundUpTime, GridSimTags.INSIGNIFICANT);

        return true;
    }

    /**
     * Allocates a Gridlet requiring multiple PEs into a free PEs and sets the Gridlet status into
     * INEXEC and PEs status into busy afterwards.
     * @param rgl  a ResGridlet object
     * @param numPE  number of PEs required by gridlet
     * @return <tt>true</tt> if there are empty PEs to process this Gridlet,
     *         <tt>false</tt> otherwise
     * @pre rgl != null
     * @post $none
     */
    private boolean allocatePEtoGridlet(ResGridlet rgl, int numPE) {
        // allocate the gridlet to machine(s) so that the numPE is satisfied

        MachineList machines = resource_.getMachineList();
        int allocate = numPE;
        int peIndex = -1;
        LinkedList<Integer> PEs = new LinkedList();

        for (int i = 0; i < machines.size(); i++) {
            MachineWithRAM machine = (MachineWithRAM) machines.get(i);
            // cannot use such machine
            if (machine.getFailed()) {
                peIndex += machine.getNumPE();
                continue;
            }
            int usedPEs = Math.min(allocate, machine.getNumFreePE());
            if (machine.getNumFreePE() <= 0) {
                peIndex += machine.getNumPE();
                continue;
            }
            for (int j = 0; j < usedPEs; j++) {
                PEList MyPEList = machine.getPEList();
                int freePE = MyPEList.getFreePEID();

                // Allocate this gridlet to Machine and its PEs
                rgl.setMachineAndPEID(machine.getMachineID(), freePE);
                PEs.add(peIndex + freePE + 1);

                // Set allocated PEs to BUSY status
                super.resource_.setStatusPE(PE.BUSY, machine.getMachineID(), freePE);

                allocate--;
            }

            if (allocate <= 0) {
                break;
            } else {
                peIndex += machine.getNumPE();
            }
        }

        ((ComplexGridlet) rgl.getGridlet()).setPEs(PEs);
        // change Gridlet status
        rgl.setGridletStatus(Gridlet.INEXEC);

        // add this Gridlet into execution list
        gridletInExecList_.add(rgl);


        // Identify Completion Time and Set Interrupt
        int ids[] = rgl.getListMachineID();


        // Not needed when all machines have same PE rating - just for future developement
        int rating = Integer.MAX_VALUE;
        for (int i = 0; i < ids.length; i++) {
            int curr = machineRating_[ids[i]];
            if (curr < rating) {
                rating = curr;
            }
        }

        double time = forecastFinishTime(rating,
                rgl.getRemainingGridletLength());

        int roundUpTime = (int) (time + 1);   // rounding up
        rgl.setFinishTime(roundUpTime);

        //update machine usage
        Scheduler.load += (Scheduler.activePEs / Scheduler.availPEs) * (GridSim.clock() - Scheduler.last_event);
        Scheduler.classic_load += (Scheduler.classic_activePEs / Scheduler.classic_availPEs) * (GridSim.clock() - Scheduler.last_event);
        Scheduler.max_load += 1.0 * (GridSim.clock() - Scheduler.last_event);
        Scheduler.last_event = GridSim.clock();
        Scheduler.activePEs += numPE * super.resource_.getMIPSRatingOfOnePE();
        Scheduler.classic_activePEs += numPE;

        // then send this into itself
        super.sim_schedule(super.myId_, roundUpTime, GridSimTags.INSIGNIFICANT);

        return true;

    }

    /**
     * Forecast finish time of a Gridlet.
     * <tt>Finish time = length / available rating</tt>
     * @param availableRating   the shared MIPS rating for all Gridlets
     * @param length   remaining Gridlet length
     * @return Gridlet's finish time.
     * @pre availableRating >= 0.0
     * @pre length >= 0.0
     * @post $none
     */
    private double forecastFinishTime(double availableRating, double length) {
        double finishTime = (length / availableRating);

        // This is as a safeguard since the finish time can be extremely
        // small close to 0.0, such as 4.5474735088646414E-14. Hence causing
        // some Gridlets never to be finished and consequently hang the program
        if (finishTime < 1.0) {
            finishTime = 1.0;
        }

        return finishTime;
    }

    /**
     * Checks all Gridlets in the execution list whether they are finished or
     * not.
     * @pre $none
     * @post $none
     */
    private void checkGridletCompletion() {
        ResGridlet obj = null;
        int i = 0;

        // NOTE: This one should stay as it is since gridletFinish()
        // will modify the content of this list if a Gridlet has finished.
        // Can't use iterator since it will cause an exception
        while (i < gridletInExecList_.size()) {
            obj = (ResGridlet) gridletInExecList_.get(i);

            if (obj.getRemainingGridletLength() == 0.0) {

                gridletInExecList_.remove(obj);
                gridletFinish(obj, Gridlet.SUCCESS);
                continue;
            }

            i++;
        }

        // if there are still Gridlets left in the execution
        // then send this into itself for an hourly interrupt
        // NOTE: Setting the internal event time too low will make the
        //       simulation more realistic, BUT will take longer time to
        //       run this simulation. Also, size of sim_trace will be HUGE!
        if (gridletInExecList_.size() > 0) {
            // DO NOT USE THIS unless necesary - it generates TONS of internal events that SLOW DOWN the simulation terribly
            //super.sendInternalEvent(60.0*60.0);
            //super.sim_schedule(super.myId_, 60.0*60.0, GridSimTags.INSIGNIFICANT);
        }
    }

    /**
     * Updates the Gridlet's properties, such as status once a
     * Gridlet is considered finished.
     * @param rgl   a ResGridlet object
     * @param status   the Gridlet status
     * @pre rgl != null
     * @pre status >= 0
     * @post $none
     */
    private void gridletFinish(ResGridlet rgl, int status) {

        // Set PE on which Gridlet finished to FREE
        if (rgl.getNumPE() > 1) {
            int[] machines = rgl.getListMachineID();
            int[] pes = rgl.getListPEID();
            for (int i = 0; i < machines.length; i++) {
                // because gridlet's setMachineAndPEID(int machineID, int peID) (ResGridlet class)
                // generates always pair machine_ID:PE_ID into 2 arrays, these arrays have always the same length
                super.resource_.setStatusPE(PE.FREE, machines[i], pes[i]);
            }
        } else {
            super.resource_.setStatusPE(PE.FREE, rgl.getMachineID(), rgl.getPEID());
        }

        // the order is important! Set the status first then finalize
        // due to timing issues in ResGridlet class
        rgl.setGridletStatus(status);
        rgl.finalizeGridlet();
        if (status == Gridlet.FAILED_RESOURCE_UNAVAILABLE) {

            double wall = GridSim.clock() - rgl.getGridletArrivalTime();
            double actual = GridSim.clock() - rgl.getExecStartTime();
            rgl.getGridlet().setExecParam(wall, actual);
        }

        //super.sendFinishGridlet( rgl.getGridlet() );
        super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, GridSimTags.GRIDLET_RETURN, rgl.getGridlet());

        //update machine usage
        Scheduler.load += (Scheduler.activePEs / Scheduler.availPEs) * (GridSim.clock() - Scheduler.last_event);
        Scheduler.classic_load += (Scheduler.classic_activePEs / Scheduler.classic_availPEs) * (GridSim.clock() - Scheduler.last_event);
        Scheduler.max_load += 1.0 * (GridSim.clock() - Scheduler.last_event);


        Scheduler.last_event = GridSim.clock();
        Scheduler.activePEs -= rgl.getNumPE() * super.resource_.getMIPSRatingOfOnePE();
        Scheduler.classic_activePEs -= rgl.getNumPE();

        //decrease requestedPEs value
        Scheduler.requestedPEs -= rgl.getNumPE();

        used_mips += rgl.getNumPE() * rgl.getGridlet().getActualCPUTime() * this.resource_.getMIPSRatingOfOnePE();
        used_usage += rgl.getNumPE() * rgl.getGridlet().getActualCPUTime();

        // move Queued Gridlet into exec list       
        allocateQueueGridlet();
    }

    /**
     * Handles an operation of canceling a Gridlet in either execution list
     * or paused list.
     * @param gridletId    a Gridlet ID
     * @param userId       the user or owner's ID of this Gridlet
     * @param an object of ResGridlet or <tt>null</tt> if this Gridlet is not
     *        found
     * @pre gridletId > 0
     * @pre userId > 0
     * @post $none
     */
    private ResGridlet cancel(int gridletId, int userId) {
        ResGridlet rgl = null;

        // Find in EXEC List first
        int found = super.findGridlet(gridletInExecList_, gridletId, userId);
        if (found >= 0) {
            // update the gridlets in execution list up to this point in time
            updateGridletProcessing();

            // Get the Gridlet from the execution list
            rgl = (ResGridlet) gridletInExecList_.remove(found);

            // if a Gridlet is finished upon cancelling, then set it to success
            // instead.
            if (rgl.getRemainingGridletLength() == 0.0) {
                rgl.setGridletStatus(Gridlet.SUCCESS);
            } else {
                rgl.setGridletStatus(Gridlet.CANCELED);
            }

            // Set PE on which Gridlet finished to FREE
            super.resource_.setStatusPE(PE.FREE, rgl.getMachineID(),
                    rgl.getPEID());
            allocateQueueGridlet();
            return rgl;
        }

        // Find in QUEUE list
        found = super.findGridlet(gridletQueueList_, gridletId, userId);
        if (found >= 0) {
            rgl = (ResGridlet) gridletQueueList_.remove(found);
            rgl.setGridletStatus(Gridlet.CANCELED);
        } // if not, then find in the Paused list
        else {
            found = super.findGridlet(gridletPausedList_, gridletId, userId);

            // if found in Paused list
            if (found >= 0) {
                rgl = (ResGridlet) gridletPausedList_.remove(found);
                rgl.setGridletStatus(Gridlet.CANCELED);
            }

        }
        return rgl;
    }

    public void processOtherEvent(Sim_event ev) {
        if (ev == null) {
            System.out.println(resName_ + ".processOtherEvent(): " +
                    "Error - an event is null.");
            return;
        }
        if (ev.get_tag() == AleaSimTags.FAILURE_MACHINE) {
            //System.out.println(Math.round(GridSim.clock())+": "+resName_ +": starts killing machines remaining jobs="+ gridletInExecList_.size()+" failed machines now/total="+resource_.getNumFailedMachines()+"/"+resource_.getNumMachines());        

            Failure failure = (Failure) ev.get_data();
            double duration = failure.getDuration();
            int[] ids = failure.getIds();
            //Scheduler.decreasePEs(this.resId_, ids.length);
            mach_failed = true;
            setMachinesFailed(ids, duration);
            // this will cause the resource to restart

            double now = GridSim.clock();
            if (last_time == now) {
                killed_cpus += ids.length;
            // do nothing, internal event has already been sent to inform the Scheduler
            } else {
                killed_cpus = ids.length;
                last_time = now;
                super.sim_schedule(super.myId_, 0.01, AleaSimTags.FAILURE_START, null);
            }
            //super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.FAILURE_START, this.resId_);
            super.sim_schedule(super.myId_, duration, AleaSimTags.FAILURE_RESTART2, failure);

        } else if (ev.get_tag() == AleaSimTags.FAILURE_INFO) {
            double duration = (Double) ev.get_data();
            failed = true;
            setResourceFailed();
            // this will cause the resource to restart
            failure_time += duration * this.resource_.getNumPE();
            wfailure_time += duration * this.resource_.getMIPSRating();
            Scheduler.failure_time += duration * this.resource_.getNumPE();
            Scheduler.wfailure_time += duration * this.resource_.getMIPSRating();
            super.sim_schedule(GridSim.getEntityId("Alea_3.0_scheduler"), 0.0, AleaSimTags.FAILURE_START, this.resId_);
            super.sim_schedule(super.myId_, duration, AleaSimTags.FAILURE_FINISHED);

        } else {
            System.out.println("Unknown tag: " + ev.get_tag());
        }
    }

    public void setMachinesFailed(int[] ids, double duration) {
        int prev = this.resource_.getNumFailedMachines();
        MachineList list = super.resource_.getMachineList();
        int offPEs = 0;
        double offMIPS = 0.0;
        int killed = 0;
        // some gridlets may have finished so far
        updateGridletProcessing();
        String idss = "";
        for (int i = 0; i < ids.length; i++) {
            killed += setGridletsFailed(ids[i]);
            offPEs += list.getMachine(ids[i]).getNumPE();
            offMIPS += list.getMachine(ids[i]).getMIPSRating();
            list.getMachine(ids[i]).setFailed(true);
            idss += ids[i] + ",";

        }
        failure_time += duration * offPEs;
        Scheduler.failure_time += duration * offPEs;

        wfailure_time += duration * offMIPS;
        Scheduler.wfailure_time += duration * offMIPS;

        // update number of CPUs
        Scheduler.classic_availPEs -= offPEs;
        Scheduler.availPEs -= offMIPS;
        Scheduler.last_event = GridSim.clock();
        int req = 0;
        for (int i = 0; i < gridletInExecList_.size(); i++) {
            ResGridlet rgl = (ResGridlet) gridletInExecList_.get(i);
            req += rgl.getNumPE();
        }

        int failedm = this.resource_.getNumFailedMachines();
        int total = this.resource_.getNumPE();
        int mach = this.resource_.getNumMachines();
        int one = total / mach;
        int runningPE = total - (one * failedm);





    //System.out.println(Math.round(GridSim.clock())+": failure of: "+resName_ +" killed: "+ids.length+" machines. ["+ids[0]+"](x2), running: "+getNumRunning()+"/"+this.resource_.getNumPE()+" dur:"+duration);
    //System.out.println(Math.round(GridSim.clock())+": "+resName_ +": killing machines="+idss+" killed jobs="+killed+" remaining jobs="+ gridletInExecList_.size()+ 
    //        " requesting="+req+" PEs, failed machines pre/post/total="+prev+"/"+resource_.getNumFailedMachines()+"/"+resource_.getNumMachines()+" avail/allPEs="+freePE+"/"+total);        
    }

    private int getNumRunning() {
        int running = 0;
        MachineList mlist = this.resource_.getMachineList();

        for (int i = 0; i < mlist.size(); i++) {
            Machine m = mlist.getMachine(i);
            if (m.getFailed() == false) {
                running += m.getNumPE();
            }
        }
        return running;
    }

    public int setGridletsFailed(int failedMachID) {

        int killed = 0;

        ResGridlet rgl;

        // go on with the gridlet in InExec list.
        int machID;
        int[] machIDs;
        for (int i = 0; i < gridletInExecList_.size(); i++) {
            rgl = (ResGridlet) gridletInExecList_.get(i);
            if (rgl.getRemainingGridletLength() == 0.0) {
                rgl.setGridletStatus(Gridlet.SUCCESS);
            }
            if (rgl.getNumPE() > 1) {
                machIDs = rgl.getListMachineID();
            } else {
                machIDs = new int[1];
                machIDs[0] = rgl.getMachineID();
            }


            // only fail gridlets allocated to the machines which have failed
            for (int j = 0; j < machIDs.length; j++) {
                machID = machIDs[j];
                if (machID == failedMachID) {
                    int status = rgl.getGridletStatus();

                    // if the gridlet has already finished, then just send it back.
                    // Otherwise, set status to FAILED
                    if (status != Gridlet.SUCCESS) {
                        status = Gridlet.FAILED_RESOURCE_UNAVAILABLE;
                    }
                    //Scheduler.removeGi(this.resId_, ((ComplexGridlet) rgl.getGridlet()));
                    gridletFinish(rgl, status);
                    gridletInExecList_.remove(rgl);
                    i--;
                    killed++;
                    break;
                }
            }
        }
        return killed;
    }

    public void setResourceFailed() {
        int prev = resource_.getNumFailedMachines();
        for (int i = 0; i < gridletQueueList_.size(); i++) {
            ResGridlet obj = (ResGridlet) gridletQueueList_.get(i);
            int status = obj.getGridletStatus();
            if (status != Gridlet.SUCCESS) {
                status = Gridlet.FAILED_RESOURCE_UNAVAILABLE;
            }
            gridletFinish(obj, status);
        }
        gridletQueueList_.clear();

        // some gridlets may have finished so far
        updateGridletProcessing();
        for (int i = 0; i < gridletInExecList_.size(); i++) {
            ResGridlet obj = (ResGridlet) gridletInExecList_.get(i);
            if (obj.getRemainingGridletLength() == 0.0) {
                obj.setGridletStatus(Gridlet.SUCCESS);
            }
            int status = obj.getGridletStatus();
            if (status != Gridlet.SUCCESS) {
                status = Gridlet.FAILED_RESOURCE_UNAVAILABLE;
            }
            gridletFinish(obj, status);
        }
        //System.out.println(gridletInExecList_.size()+" executed job killed");
        gridletInExecList_.clear();

        for (int i = 0; i < gridletPausedList_.size(); i++) {
            ResGridlet obj = (ResGridlet) gridletPausedList_.get(i);
            int status = obj.getGridletStatus();
            if (status != Gridlet.SUCCESS) {
                status = Gridlet.FAILED_RESOURCE_UNAVAILABLE;
            }
            gridletFinish(obj, status);
        }
        gridletPausedList_.clear();

        MachineList list = super.resource_.getMachineList();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            list.getMachine(i).setFailed(true);
        }

        // update number of CPUs
        Scheduler.classic_availPEs -= this.totalPE_;
        Scheduler.availPEs -= this.totalPE_ * this.resource_.getMIPSRatingOfOnePE();
        Scheduler.last_event = GridSim.clock();
        System.out.println(resName_ + ": all jobs killed, machines pre/post/total=" + prev + "/" + resource_.getNumFailedMachines() + "/" + resource_.getNumMachines());

    }

    public int getNumFreePE() {
        int free = 0;
        MachineList mlist = this.resource_.getMachineList();
        if (ExperimentSetup.failures) {
            for (int i = 0; i < mlist.size(); i++) {
                Machine m = mlist.getMachine(i);
                if (m.getFailed() == false) {
                    free += m.getNumFreePE();
                }
            }
        } else {
            free = this.resource_.getNumFreePE();
        }
        return free;
    }
} // end class

