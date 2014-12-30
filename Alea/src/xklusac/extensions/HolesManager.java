/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.extensions;


import gridsim.MachineList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import xklusac.environment.GridletInfo;
import xklusac.environment.MachineWithRAM;

/**
 * This class manages holes in schedule of ResourceInfo. Every machine has list
 * of holes, which are sorted by start time. Holes are always in following
 * order. It can't happen that one hole finish time is greater than start time
 * of following hole.
 *
 * @author Jiri Oliva
 */
public class HolesManager {

    private Map<Integer, LinkedList<Hole>> holesDB = null;
    Integer peRating;

    /**
     * Constructor method - creates initial holes for given machines.
     *
     * @param startTime
     * @param machines of ResourceInfo
     * @param peRating of ResourceInfo
     */
    public HolesManager(Double startTime, MachineList machines, Integer peRating) {
        holesDB = new LinkedHashMap<Integer, LinkedList<Hole>>();
        this.peRating = peRating;
        for (int i = 0; i < machines.size(); i++) {
            MachineWithRAM machine = (MachineWithRAM) machines.get(i);
            holesDB.put(machine.getMachineID(), new LinkedList<Hole>());
            holesDB.get(machine.getMachineID()).add(Hole.createLastInfiniteHole(startTime, machine));
        }
    }

    /**
     * This method updates initial infinity holes with given gridlet, which is
     * already running in the system.
     *
     * @param currentTime
     * @param finishTime
     * @param gi gridlet
     * @param machineID
     */
    public void updateInitialHolesWithGridletInExec(double currentTime, double finishTime, GridletInfo gi, Integer machineID) {

        //dont update holes for gridlet, which is already finished
        if (finishTime == currentTime) {
            return;
        }

        LinkedList<Hole> holes = holesDB.get(machineID);
        for (Hole hole : holes) {
            if (hole.isInfinityLastGap()) {
                Hole giHole = splitInfinityHole(hole, gi, finishTime);
                holesDB.get(machineID).addLast(giHole);
                break;

            } else {

                if (finishTime <= hole.getEnd()) {
                    Hole giHole = splitHole(hole, gi, finishTime);
                    if (giHole != null) {
                        holesDB.get(machineID).addLast(giHole);
                    }
                    break;

                } else {
                    hole.setRam(hole.getRam() - gi.getRam());
                    hole.setSize(hole.getSize() - gi.getPpn());
                }
            }
        }
        sortHolesByStartTime();
    }

    /**
     * Update infinity hole with gridlet. This method will create new hole,
     * where given gridlet is started, and update start time of infinity hole.
     *
     * @param infinityHole
     * @param gi gridlet
     * @param finishTime
     * @return new hole occupied by the gridlet
     */
    private Hole splitInfinityHole(Hole infinityHole, GridletInfo gi, Double finishTime) {
        Hole previousHole = infinityHole.createCopy();
        infinityHole.setStart(finishTime);

        previousHole.setEnd(finishTime);
        previousHole.calculateLengthAndMIPS(peRating);
        previousHole.setRam(infinityHole.getRam() - gi.getRam());
        previousHole.setSize(infinityHole.getSize() - gi.getPpn());
        previousHole.setInfinityLastGap(false);

        return previousHole;
    }

    /**
     * Update simple hole with gridlet. If gridlet length is larger or equal
     * than this hole, then only update resources of this hole. If gridlet
     * length is shorter, then create new hole, where given gridlet is started,
     * and also update the second hole.
     *
     * @param hole
     * @param gi gridlet
     * @param finishTime
     * @return new hole if created
     */
    private Hole splitHole(Hole hole, GridletInfo gi, Double finishTime) {
        if (finishTime.compareTo(hole.getEnd()) == 0) {
            hole.setRam(hole.getRam() - gi.getRam());
            hole.setSize(hole.getSize() - gi.getPpn());

            return null;
        } else {

            Hole nextHole = hole.createCopy();
            nextHole.setStart(finishTime);
            nextHole.calculateLengthAndMIPS(peRating);

            hole.setEnd(finishTime);
            hole.calculateLengthAndMIPS(peRating);
            hole.setRam(hole.getRam() - gi.getRam());
            hole.setSize(hole.getSize() - gi.getPpn());

            return nextHole;
        }

    }

    /**
     * This method will find earliest start time for gridlet - used by schedule
     * based algorithms. It always search for one primary hole, which can be
     * aggregated with following holes on same machine or united with another
     * holes on other machines. In the end this method returns list of earliest
     * holes, which can start this gridlet, or empty list if gridlet can not be
     * started.
     *
     * @param gi gridlet
     * @param jobMIPS job length
     * @param resID resourceInfo ID
     * @return list of holes
     */
    public ArrayList<Hole> findEarliestStartTime(GridletInfo gi, Double jobMIPS, int resID) {
        ArrayList<Hole> candidates = new ArrayList<Hole>();
        int numNodes = gi.getNumNodes();
        Double earliestTime = Double.MAX_VALUE - 10;
        for (Integer machineID : holesDB.keySet()) {
            //search all holes on all machines
            LinkedList<Hole> holes = holesDB.get(machineID);
            for (Hole hole : holes) {
                boolean success = false;
                if (hole.getStart() >= earliestTime) {
                    //stop searching when this hole start time is greater than earliest time
                    break;
                }
                if (hole.isSuitableForGridlet(gi, jobMIPS)) {
                    //this hole is large enough for gridlet
                    success = true;

                } else {
                    if (hole.isShortForGridlet(gi, jobMIPS)) {
                        Integer holeIndex = holes.indexOf(hole);
                        //this hole has enough sources, but length is short
                        success = tryAggregateFollowingHoles(gi, holeIndex, machineID, jobMIPS, hole.getEnd());
                    }
                }
                if (success && numNodes <= 1) {
                    //we found primary hole for gridlet, which needs only one machine
                    if (hole.getStart() < earliestTime) {
                        earliestTime = hole.getStart();
                        candidates.clear();
                        candidates.add(hole);
                    }
                }

                if (success && numNodes > 1) {
                    //we found primary hole, now try to find holes on other machines
                    double startTime = hole.getStart();
                    //calculate maximal finish time of primary hole
                    double maxFinishTime = findLongestLengthOfHole(gi, holes.indexOf(hole), machineID, hole.getEnd());
                    ArrayList<Hole> otherNodes;
                    ArrayList<Hole> finalNodes;
                    //find every hole, which can be united with primary hole and start the gridlet
                    otherNodes = findOtherNodesSuitableForGridlet(gi, machineID, maxFinishTime, startTime, jobMIPS);
                    //we have list of suitable holes, find final list of holes, which can start the gridlet
                    finalNodes = findMinimalUniteHolesForGridlet(gi, hole, otherNodes, maxFinishTime, jobMIPS);

                    if (finalNodes != null) {
                        finalNodes.add(hole);
                        Double uniteStartTime = findEarliestUnitedStartTime(finalNodes);
                        if (uniteStartTime < earliestTime) {
                            earliestTime = uniteStartTime;
                            candidates.clear();
                            candidates.addAll(finalNodes);
                        }
                    }

                }
            }
        }

        return candidates;
    }

    /**
     * This method takes list of holes and tries to find 
     * prossible united way to run given gridlet, which needs more nodes.
     * Holes are sorted by start time, then one by one put to final list. If new hole
     * breaks unification of final list, then first hole in the list is removed.
     * Returns final list of primary holes (each on different machine) or null, if 
     * unification is not suitable for gridlet.
     * 
     * @param gridlet
     * @param primaryHole
     * @param nodes
     * @param maxTime
     * @param job length
     * @return final list
     */
    private ArrayList<Hole> findMinimalUniteHolesForGridlet(GridletInfo gi, Hole primaryHole, ArrayList<Hole> nodes, Double maxTime, Double jobMIPS) {
        ArrayList<Hole> finalNodes = new ArrayList<Hole>();
        int numNodes = gi.getNumNodes() - 1;
        Collections.sort(nodes, new StartTimeHoleComparator());

        for (Hole hole : nodes) {
            finalNodes.add(hole);
            //every possible hole has enough sources for gridlet, but length of united hole can be short
            boolean success = isLengthOfUnitedHoleSuitableForGridlet(finalNodes, primaryHole, maxTime, jobMIPS);
            if (success && numNodes == finalNodes.size()) {
                return finalNodes;
            }

            if (success == false && finalNodes.size() > 1) {
                finalNodes.remove(0);
            }

        }

        return null;
    }

    /**
     * This method tests if length of united hole is long enough to run given gridlet.
     * 
     * @param nodes
     * @param primaryHole
     * @param maxTime
     * @param jobMIPS job length
     * @return true if length is long enough
     */
    private boolean isLengthOfUnitedHoleSuitableForGridlet(ArrayList<Hole> nodes, Hole primaryHole, Double maxTime, Double jobMIPS) {
        double uniteStartTime = primaryHole.getStart();
        double uniteFinishTime = maxTime;
        double nodeMips;

        //find maximal start time and minimal finish time
        for (Hole hole : nodes) {
            if (hole.getStart() >= uniteStartTime) {
                uniteStartTime = hole.getStart();
            }
            if (hole.getMaxEnd() <= uniteFinishTime) {
                uniteFinishTime = hole.getMaxEnd();
            }
        }

        //test length of the united hole
        if (uniteStartTime < uniteFinishTime) {
            nodeMips = uniteFinishTime - uniteStartTime;
            return nodeMips >= jobMIPS;
        }

        return false;
    }

    /**
     * This method finds every possible hole on other machines, which can run givne gridlet 
     * and can be also united with our primary hole (defined by maxTime and minTime).
     * We always calculate maximal finish time of holes, because there is possibility that
     * aggregated hole can run the gridlet. Suitable hole can be not present in the list, if
     * this hole was already calculated by aggregation of previous hole.
     * 
     * @param gi gridlet
     * @param machineID to skip
     * @param maxTime
     * @param minTime
     * @param jobMIPS job length
     * @return list of suitable holes for unification
     */
    private ArrayList<Hole> findOtherNodesSuitableForGridlet(GridletInfo gi, int skipMachine, Double maxTime, Double minTime, Double jobMIPS) {
        ArrayList<Hole> otherNodes = new ArrayList<Hole>();
        
        //search all holes on all machines expect primary hole machine
        for (Integer machineID : holesDB.keySet()) {
            if (machineID == skipMachine) {
                continue;
            }
            Double previousMaxTime = -1.0;
            LinkedList<Hole> holes = holesDB.get(machineID);
            for (Hole hole : holes) {
                boolean success = false;
                if (hole.getStart() <= previousMaxTime) {
                    //ignore holes, which were already aggregated by previous hole
                    continue;
                }

                if (hole.isSuitableForGridlet(gi, jobMIPS)) {
                    //this hole is large enough
                    success = true;

                } else {
                    if (hole.isShortForGridlet(gi, jobMIPS)) {
                        Integer holeIndex = holes.indexOf(hole);
                        //this hole have resources, but is short. Try aggregation
                        success = tryAggregateFollowingHoles(gi, holeIndex, machineID, jobMIPS, hole.getEnd());
                    }
                }

                if (success) {
                    double startTimeNode = hole.getStart();
                    double maxFinishTimeNode = findLongestLengthOfHole(gi, holes.indexOf(hole), machineID, hole.getEnd());
                    //we found possible hole for unification with primary hole
                    if (isNextNodeForGridlet(startTimeNode, maxFinishTimeNode, maxTime, minTime, jobMIPS)) {
                        hole.setMaxEnd(maxFinishTimeNode);
                        otherNodes.add(hole);
                        previousMaxTime = maxFinishTimeNode;

                    }

                }
            }
        }

        return otherNodes;
    }

    /**
     * This method tests if hole, which can run given gridlet, can also be united 
     * united with primary hole and still run this gridlet.
     * 
     * @param startTimeNode
     * @param maxFinishTimeNode
     * @param minTime
     * @param maxTime
     * @param jobMIPS job length
     * @return 
     */
    private boolean isNextNodeForGridlet(double startTimeNode, double maxFinishTimeNode, double minTime, double maxTime, double jobMIPS) {
        double uniteMinTime;
        double uniteMaxTime;
        double nodeMips;
        
        //find maximal start time and minimal finish time
        if (startTimeNode <= minTime) {
            uniteMinTime = startTimeNode;
        } else {
            uniteMinTime = minTime;
        }
        if (maxFinishTimeNode >= maxTime) {
            uniteMaxTime = maxFinishTimeNode;
        } else {
            uniteMaxTime = maxTime;
        }

        nodeMips = uniteMaxTime - uniteMinTime;
        return nodeMips >= jobMIPS;
    }

    /**
     * This method tries to aggregate given hole with following holes on machine.
     * Searching is stopped when following hole has not enough resources to run
     * given gridlet or its start time is greater than previous hole.
     * 
     * @param gi gridlet
     * @param holeIndex
     * @param machineID
     * @param jobMIPS job length
     * @param endTime
     * @return true if aggregated hole is large enough
     */
    private boolean tryAggregateFollowingHoles(GridletInfo gi, Integer holeIndex, Integer machineID, double jobMIPS, double endTime) {
        LinkedList<Hole> holes = holesDB.get(machineID);
        Double holeMIPS = 0.0;
        for (int i = holeIndex; i < holes.size(); i++) {
            Hole nextCandidate = holes.get(i);
            //search only following holes
            if (nextCandidate.getStart() <= endTime) {
                if (nextCandidate.hasEnoughSourcesForGridlet(gi)) {
                    if (nextCandidate.isInfinityLastGap()) {
                        holeMIPS = Double.MAX_VALUE - 10;
                    } else {
                        holeMIPS += nextCandidate.getMips();
                        endTime = nextCandidate.getEnd();
                    }

                }
                if (holeMIPS >= jobMIPS) {
                    //aggregated hole is large enough
                    return true;
                }

            } else {
                //aggregation was not large enough
                return false;
            }
        }
        return false;
    }

    /**
     * This method returns maximal length for given hole. Maximal length is calculated 
     * with aggregation of following holes and still run the gridlet.
     * 
     * @param gi gridlet
     * @param holeIndex
     * @param machineID
     * @param endTime
     * @return maximal finish time
     */
    private double findLongestLengthOfHole(GridletInfo gi, Integer holeIndex, Integer machineID, double endTime) {
        LinkedList<Hole> holes = holesDB.get(machineID);
        Double maximalFinishTime = 0.0;

        for (int i = holeIndex; i < holes.size(); i++) {
            Hole nextCandidate = holes.get(i);
            //search only following holes
            if (nextCandidate.getStart() <= endTime) {
                if (nextCandidate.hasEnoughSourcesForGridlet(gi)) {
                    if (nextCandidate.isInfinityLastGap()) {
                        maximalFinishTime = Double.MAX_VALUE - 10;
                    } else {
                        maximalFinishTime = nextCandidate.getEnd();
                    }
                }
            } else {
                //this hole cannot be aggregated, return calculated finish time
                return maximalFinishTime;
            }
        }

        return maximalFinishTime;
    }

    /**
     * This method updates holes in schedule with new gridlet. Given list of holes 
     * consists of primary holes on different machines. Update every primary hole and
     * if length of gridlet is longer than the hole, then also update following holes 
     * until length is long enough.
     * 
     * @param nodesToUpdate
     * @param gi gridlet
     * @param jobMIPS job length
     * @return united start time
     */
    public double updateHolesWithNewGridlet(ArrayList<Hole> nodesToUpdate, GridletInfo gi, double jobMIPS) {
        Double startTime = Double.MAX_VALUE - 10;
        startTime = findEarliestUnitedStartTime(nodesToUpdate);

        //update every machine, which will run new gridlet
        for (Hole node : nodesToUpdate) {
            Double timeRemaining = jobMIPS;

            LinkedList<Hole> holes = holesDB.get(node.getMachineID());
            int holeIndex = holes.indexOf(node);
            for (int i = holeIndex; i < holes.size(); i++) {
                Hole holeToUpdate = holes.get(i);
                //calculate finish time of gridlet
                Double finishTime = holeToUpdate.getStart() + timeRemaining;
                timeRemaining -= holeToUpdate.getMips();
                
                if (holeToUpdate.isInfinityLastGap()) {
                    //this hole is last on machine, update it and move to another machine
                    Hole newHole = splitInfinityHole(holeToUpdate, gi, finishTime);
                    holes.add(newHole);
                    break;
                    
                } else {
                    if (finishTime <= holeToUpdate.getEnd()) {
                        //update hole and create new hole, if gridlet ends earlier than this hole
                        Hole newHole = splitHole(holeToUpdate, gi, finishTime);
                        if (newHole != null) {
                            holesDB.get(newHole.getMachineID()).add(newHole);
                        } else {
                            break;
                        }

                    } else {
                        //gridlet is longer, update resources and keep updating following holes
                        holeToUpdate.setRam(holeToUpdate.getRam() - gi.getRam());
                        holeToUpdate.setSize(holeToUpdate.getSize() - gi.getPpn());
                    }
                }

                //stop updating if length of aggregated holes is larger than job length
                if (Double.compare(timeRemaining, 0.00) <= 0) {
                    break;
                }
            }
            
            sortHolesByStartTime();

        }
        return startTime;
    }

    /**
     * This method removes holes, which have zero PEs.
     */
    public void removeFullHoles() {
        for (Integer machineID : holesDB.keySet()) {
            LinkedList<Hole> holes = holesDB.get(machineID);
            for (int i = 0; i < holes.size(); i++) {
                if (holes.get(i).getSize() <= 0) {
                    holes.remove(i);
                }
            }
        }
    }

    /**
     * Sort hole list on every machine by start time.
     */
    public void sortHolesByStartTime() {
        for (LinkedList<Hole> holes : holesDB.values()) {
            Collections.sort(holes, new StartTimeHoleComparator());
        }
    }

    /**
     * This method returns earliest united time of given holes
     * 
     * @param holes
     * @return earliest united time
     */
    static public Double findEarliestUnitedStartTime(ArrayList<Hole> holes) {
        Double min = Double.MAX_VALUE - 10;
        for (Hole hole : holes) {
            if (hole.getStart() <= min) {
                min = hole.getStart();
            }
        }

        return min;
    }

    
    /**
     * Print statistics of all holes.
     */
    public void printValuesOfHoles() {
        for (LinkedList<Hole> holes : holesDB.values()) {
            System.out.println("printing all holes on machine ID = " + holes.getFirst().getMachineID());

            for (Hole hole : holes) {
                System.out.println("PEs= " + hole.getSize() + " freeRAM= " + hole.getRam() + " startTime= " 
                        + hole.getStart() + " finishTime= " + hole.getEnd() +  " isLast= " + hole.isInfinityLastGap());
            }
        }

    }
}
