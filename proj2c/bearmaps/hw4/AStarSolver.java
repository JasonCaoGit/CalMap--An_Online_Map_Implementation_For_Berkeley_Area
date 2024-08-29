package bearmaps.hw4;

import bearmaps.hw4.ArrayHeapMinPQ;
import bearmaps.proj2ab.DoubleMapPQ;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.PriorityQueue;

//Author: Zhuoyuan Cao

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {

    private SolverOutcome outcome;
    private double solutionWeight;
    private List<Vertex> solution;
    private double timeSpent;
    private double explorationTime;
    private int numStatesExplored;
    private HashMap<Vertex, Double> distTo;
    private HashMap<Vertex, Vertex> edgeTo;
    private AStarGraph<Vertex> graph;

    private ArrayHeapMinPQ<Vertex> PQ;
    /*
     * First create a priority queue
     * then add the current source to the PQ
     * start dequeing the PQ until PQ gets empty or PQ.getSmallest is the goal(found solution), or time out exceeded
     *   in the loop do this
     * p = pq.removeSmallest
     * Get all neighbors, which are edges from the vertex
     * and relax them all
     *
     *
     *
     *
     *
     * */
    public AStarSolver(AStarGraph<Vertex> graphGiven, Vertex start, Vertex goal, double timeOut) {
        PQ = new ArrayHeapMinPQ<>();
        PQ.add(start, 0);
        distTo = new HashMap<>();
        edgeTo = new HashMap<>();
        distTo.put(start, 0.0);
        graph = graphGiven;
        //count the time
        Stopwatch sw = new Stopwatch();
        timeSpent = 0;
        solution = new ArrayList<>();
        //only run when three conditions holds at the same time
        /*
         * PQ have members
         * Time spent less than time out
         * getSmallest has not been goal yet, which means not solved
         * */

        /*   * start dequeing the PQ until PQ gets empty or PQ.getSmallest is the goal(found solution), or time out exceeded
         *   in the loop do this
         * p = pq.removeSmallest
         * Get all neighbors, which are edges from the vertex
         * and relax them all*/

        while (PQ.size() > 0 && timeSpent < timeOut && !PQ.getSmallest().equals(goal)) {
            Vertex p = PQ.removeSmallest();
            List<WeightedEdge<Vertex>> neighbors = graph.neighbors(p);

            for (WeightedEdge<Vertex> e : neighbors) {
                relaxEdge(PQ, e, goal);
                timeSpent = sw.elapsedTime();
            }
            numStatesExplored++;

        }

        //If time out is the result
        if (timeOut < timeSpent) {
            outcome = SolverOutcome.TIMEOUT;
            timeSpent = sw.elapsedTime();
            solutionWeight = -1;
            return;

        }

        //if unsolvable
        if (PQ.size() == 0) {
            outcome = SolverOutcome.UNSOLVABLE;
            timeSpent = sw.elapsedTime();
            solutionWeight = -1;
            return;
        }

        //if priority.getSmallest is the goal
        /*
        *
        * */
        if (PQ.getSmallest().equals(goal)) {
            outcome = SolverOutcome.SOLVED;

            //add all the edges from goal to the start
            Vertex vertex = edgeTo.get(goal);
            solution.add(goal);
            solution.add(0,vertex);
            while (!vertex.equals(start)) {
                vertex = edgeTo.get(vertex);
                solution.add(0,vertex);
            }
           solutionWeight = distTo.get(goal);
            timeSpent = sw.elapsedTime();
            return;


        }







    }

    /*
    * Get two vertex pFrom and qTo
    * Get the weight of the edge
    * if distTo[qTo] does not exist, add distTo[pFrom] + w to distTo[qTo]
    * and add p to edgeTo edgeTo[qTo]
    * if already exists, distTo[qTo],
    * and second condition, only if distTo[qTo] > distTo[pFrom] +w:
    * update distTo[q], update edgeTo[qTo]
    * After done this,
    *
    * if q in PQ change its priority to distTo[q] + heuristic
    * if not add this to PQ
    *
    *
    *
    *
    * */

    //distTo format vertex ->bestknowdistance
    //edgeTo format vertex - > the best vertex to the vertex
    public void relaxEdge(ArrayHeapMinPQ<Vertex> PQ, WeightedEdge<Vertex> neighbor, Vertex goal) {
        Vertex pFrom = neighbor.from();
        Vertex qTo = neighbor.to();
        double weight = neighbor.weight();
        //This has to exist right?
        double distanceToP = distTo.get(pFrom);
        //if distTo does not contain it
        if (!distTo.containsKey(qTo)) {
            //relax and update PQ
            distTo.put(qTo, weight + distanceToP);
            edgeTo.put(qTo, pFrom);

            //get the heuristic of qTo and goal
            double heuristic = graph.estimatedDistanceToGoal(qTo, goal);
            double distanceToQ = distTo.get(qTo);
            updatePQ(qTo, distanceToQ +heuristic );

        } else {
            if(distTo.get(qTo) > distanceToP + weight) {
                distTo.put(qTo, weight + distanceToP);
                edgeTo.put(qTo, pFrom);
                double heuristic = graph.estimatedDistanceToGoal(qTo, goal);
                double distanceToQ = distTo.get(qTo);
                updatePQ(qTo, distanceToQ +heuristic);

            }


        }




    }


    public void updatePQ(Vertex vertex, double priority) {
        if (PQ.contains(vertex)) {
            PQ.changePriority(vertex, priority);
        } else {
            PQ.add(vertex, priority);
        }
    }



    //TODO Set the exploration time in the solver done
    public double explorationTime() {
        return timeSpent;

    }


    /*
     * TODO upby 1 everytime you dequeue the PQ done
     * */
    public int numStatesExplored() {
        return numStatesExplored;
    }

    /*
     * TODO calculate the weight of the edges done
     * */
    public double solutionWeight() {
        return solutionWeight;
    }


    //TODO update the outcome

    /*
     *
     * Solved if the solver reach the goal in the getSmallest method of PQ
     * Unsolvable is when the priority queue becomes empty after you relax all edges in the current
     * Timeout means the solver runs out of time
     * */
    public SolverOutcome outcome() {
        return outcome;
    }

    //TODO update the solution when solving
    //empty if result is timeout or unsolvable
    public List<Vertex> solution() {
        return solution;
    }

}