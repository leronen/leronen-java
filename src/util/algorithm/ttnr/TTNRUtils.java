package util.algorithm.ttnr;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.CollectionUtils;
import util.StringUtils;
import util.Timer;

import util.collections.ArrayStack;
import util.collections.BinaryHeap;
import util.collections.Distribution;
import util.collections.HashMultiSet;
import util.collections.HashWeightedSet;
import util.collections.UnorderedPair;
import util.collections.WeightedSet;
import util.collections.graph.defaultimpl.Dijkstra;
import util.comparator.ByStringComparator;
import util.comparator.ReverseComparator;
import util.dbg.Logger;


/** 
 * A replacement for the inefficient ttnrutils based on the biomine.db graph
 * implementation. Todo: should rename Graph to DBGraph?
 * @author leronen
 *
 * TODO: k-terminal reliability. Let's use bitsets to represent the 
 * terminal sets...
 *
 * - an unexpected problem turned out to be the fact that most (at least 2/3)
 * of time was spent on generating random numbers for realizing the edges.
 *
 * - Note that an attempt to recycle a set of rand vals in [0..1[ (generated
 * a table with niters elements, read them starting from a position 
 * dictated by the number of the iteration) failed pathetically,
 * doubling the observed variance.
 * 
 * - note that java.util.random is hopelessly slow for our purposes, so 
 *   we use a crude method observed in the internet, and do this using 
 *   integers for efficiency
 *   
 *   
 *  Another way of simulation: sample a set of edges according to their probs
 *  (can sampling without replacement from an arbitrary distribution be done
 *  efficiently???) iterate edges: for each e, combine endpoints of e into a 
 *  single connected component. Hmm, it seems
 *  that fibonacci heaps etc could be ideal for this kind of approach!
 *  End after getting (1) a desired number of connected components or (2) a 
 *  desired number of edges ne. at least (2) is conceptually about the same as 
 *  scaling down the prob of each edge so that the expected number of edges
 *  is ne (avg edge prob is ne/|E|.
 *    
 * 
 */
public class TTNRUtils {
   
   
    /** For all nodes, set reached to false*/
    public static <T> void resetNodes(TTNRGraph<T> pGraph) {
                
        for (TTNRNode<T> n: pGraph.getNodes()) {            
            n.reached = false;                           
        }                                                             
    }
    
    /**
     * Set decided for each edge, according to the prob.
     * Todo: precompute the decisions for each edge?
     */
    public static <T> void realizeEdges(Collection<TTNREdge<T>> pEdges) {
                                    
        for (TTNREdge<T> e: pEdges) {
            // a unelegant but efficient and seemingly satisfactory solution
            // for randomizing the edges:
            e.attributes.exists = myrand() < e.attributes.prob_int;            
            // both of the alternatives below were found to be very slow:
            // e.exists = RandUtils.sampleBernoulli(e.prob);
            // e.exists = Math.random() < e.prob;
        }                                                             
    }
    
    private static long next = new java.util.Random().nextInt();
    
   /**
    * An unelegant but efficient and seemingly satisfactory solution
    * for randomizing the edges. This is presimably a somewhat common, in some 
    * aspects sub-optimal solution, as can be guessed from it's plain looks.
    * 
    * Returns an integer in [0,Integer.MAX_VALUE].
    * 
    * TODO: inline myrand() for efficiency? Done already by compiler/virtual machine?
    */
    public static final int myrand() {
        next = next * 1103515245 + 12345;
        int result = (int)next;
        if (result < 0) {
            return -result;
        }
        else {
            return result;
        }      
    }
    
    /**
     * Compute ttnr between a pair of nodes. Randomize edges on the fly.
     * 
     * Here, we aim for an as efficient as possible impl for this basic task.
     * 
     * It was noted that sorting the edges by the distance of the head node
     * to the target node improves performance: 13s => 10s for a large reliability
     * network (0.9) and, surprisingly as good improvement, 15s => 12s also 
     * for a low-reliability (0.07) network
     * 
     * Number of tested edges reduces from 6M to 5M (large rel) and 6.3M to 5.5M
     * (small rel), respectively.
     * 
     * It did not help (a bit on the contrary) to consider the edge length
     * in sorting edges by shortest t-distance.
     * 
     * Inlining of random number generation did not help (it even seemed
     * to decrease efficiency by about 5%).
     * 
     * In the current implementation, a proper priority queue based
     * implementation for the bestpath heuristic fails to decrease running
     * times (but instead succeeds in swiftly increasing them). However,
     * it more than halves the number of tested edges in the large-reliability
     * graph (small rel graph to be tested).
     *
     * UHH: and the biggest surprise is: using "longest edge first" as the 
     * edge sorting heuristic gives by far the best performance (clearly better
     * than "best path" heuristic...). Some figures for different edge sorting
     * heuristics in dfs:
     *   - best edge first:
     *      * 24s
     *      * tested edges: 100160313
     *      * failed edges : 35522999
     *      * working edges: 64637314
     *   
     *   - worst edge first (sic!):
     *      * 7.2s (siiiic)
     *      * tested edges: 35871917
     *      * failed edges:  12479865
     *      * working edges: 23392052
     *  
     *  - best path first:
     *       * 11s
     *       * tested edges: 51590268
     *       * failed edges: 17837831
     *       * working edges: 33752437
     *        
     *   - worst path first:
     *      * 20s 
     *      * tested edges: 83826995
     *      * failed edges: 29882056
     *      * working edges: 53944939
     *
     *    -no heuristic:
     *      
     *  The ratio of failing/worked edges tested is almost identical?!?!?
     *  DOES NOT SEEM TO MAKE SENSE!!! Miksi nyt huonoimman reliabiliteetin
     *  kaarien testaaminen ensin parantaa tulosta?!?
     *  
     *  Argh, onko kyse vain siitä, että tuolloin haku on enemmän "leveyssuuntainen"?
     *  
     *  Raadollinen totuus (?): pienipainoiset kaaret vievät korkean asteen
     *  (ja siten korkean reliabiliteetin) solmuihin, ollen siten hyviä kandidaatteja...
     *  
     *  No niin, tuolla huonomman reliabiliteetin verkolla nyt sitten 
     *  näyttääkin jo siltä, että tuo "worst edge" heuristiikka oikeastaan
     *  huonontaa tulosta, tai ei ainakaan juuri vaikuta siihen.
     *  Siellä taas "best path" näyttää parhaalta.
     *  
     *  Summa summarum: olisiko tuo "best path" nyt sitten robustein heuristiikka,
     *  vaikkakaan ei näemmä optimaalinen ainakaan tuolla yhdellä korkean
     *  ttnr:n verkolla (toki syynä voi olla muukin kuin juuri tuo ttnr)
     *  
     *  Ja vielä (UUUUH!!!) tehtiin nyt ttnr(t)-heuristiikkakin => toimii
     *  paljon huonommin kuin muut vaihtoehdot, mutta miksi? - no siksi,
     *  että ttnr näyttää useimmille lähtösolmuille olevan täysin tasaisesti
     *  jakautunut, eikä siten tarjoa juuri mitään informaatiota!
     * 
     */
    public static final <T> double computeTTNR_pairwise(TTNRGraph<T> pGraph,                                                               
                                                        T pSrc,
                                                        T pTgt,
                                                        int pNumIters) {
        
        // Timer.startTiming("computeTTNR_pairwise");
        
        TTNRNode<T> s = pGraph.getNode(pSrc);
        TTNRNode<T> t = pGraph.getNode(pTgt);
        
        if (s == null) {
            throw new RuntimeException("No such node: "+pSrc);
        }
        
        if (t == null) {
            throw new RuntimeException("No such node: "+pTgt);
        }
        
        // first, compute shortest path from t to each node, using Dijkstra        
        Timer.startTiming("Dijkstra preprocessing");
        Logger.info("Dijkstra preprocessing");
        Dijkstra.computeDistances(pGraph, t);        
        Timer.endTiming("Dijkstra preprocessing");
        
//        Timer.startTiming("Initial ttnr");
//        Logger.info("Computing reliability to t for 1000 iterations");
//        computeTTNR_singlesource(pGraph, pTgt, 1000);                
//        Timer.endTiming("Initial ttnr");
        
        Timer.startTiming("Sorting edges");
        Logger.info("Sorting edges");
        for (TTNRNode<T> v: pGraph.getNodes()) {
//            v.sortEdgesAccordingToTargetTTNR(true); // "best ttnr first"
            v.sortEdgesAccordingToTargetDistance(false); // "best t-path first" (most robust?)
//            v.sortEdgesAccordingToLen(true); // "worst edge first"
//            v.sortEdgesAccordingToLen(false); // "best edge first"                                  
        }
                                
        Timer.endTiming("Sorting edges");              
        
        Timer.startTiming("MC estimation");
        Logger.info("Actual MC estimation");
        
        // first, clear all node attributes, to be on the safe side:
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.clearAttributes();
        }        
        
        // Let's implement dfs with our own stack (to avoid recursion in the java stack...)
        ArrayStack<TTNRNode<T>> stack = new ArrayStack();                
        List<TTNRNode<T>> encounteredList = new ArrayList();
                
        int successCount = 0;
        int nTestedEdges = 0;
        int nFailedEdges = 0;
        int nWorkingEdges = 0;
                               
        boolean pathFound;        
        
        for (int i=0; i<pNumIters; i++) {                                       
            pathFound = false;
            
            if (s == t) {
                // a special case: a path from node to itself is always assumed to exist
                pathFound = true;
            }
            else if (s.dist == Double.MAX_VALUE) {
                // not reachable (as found out by dijkstra)
                pathFound = false;
            }
            else {
                // the general case. 
                
                // first, clear the encountered flag of nodes encountered in the last round 
                for (TTNRNode<T> v: encounteredList) {
                    v.reached = false;
                }
                encounteredList.clear();
                stack.clear();

                s.reached = true;
                encounteredList.add(s);
                stack.push(s);
            
                while (!pathFound && stack.size() > 0) {
                    TTNRNode<T> u = stack.pop();
                    List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                    int l = edges.size();
                    for (int j=0; j<l; j++) {
                        TTNREdge<T> e = edges.get(j);                                                        
                        TTNRNode<T> v = e.tgt;                            
                                                
                        if (!v.reached) {
                            // only have to test for existence of e if v 
                            // is not yet encountered; check it now:
                             
                            nTestedEdges++;                                                       
                            
                            if (myrand() >= e.attributes.prob_int) {
                                // edge failure
                                nFailedEdges++;                                
                            }
                            else {
                                // edge in working order
                                nWorkingEdges++;
                                v.reached = true;
                                encounteredList.add(v);
                                stack.push(v);
                                if (v == t) {
                                    // Found t!
                                    pathFound = true;
                                    break;
                                }
                            }
                        }                                                       
                    }
                }                                              
            }
            
            if (pathFound) {
                 successCount++;
            }

        }
        
        Logger.info("Number of tested edges:  "+nTestedEdges);
        Logger.info("Number of failed edges:  "+nFailedEdges);
        Logger.info("Number of working edges: "+nWorkingEdges);
        
        Timer.endTiming("MC estimation");
        // Timer.endTiming("computeTTNR_pairwise");
        
        return ((double)successCount) / pNumIters;
              
    }
    
    /**
     * Compute expected shortest path distance for a random graph.
     * Disregard iterations where there is no path at all.
     */
    public static final <T> ESPDResult 
            expectedShortestPathDistance_numedges(TTNRGraph<T> pGraph,                                                               
                                                  T pSrc, T pTgt,
                                                  int pNumIters) {
        // 
        TTNRNode<T> s = pGraph.getNode(pSrc);
        TTNRNode<T> t = pGraph.getNode(pTgt);
        
        if (s == null) {
            throw new RuntimeException("No such node: "+pSrc);
        }
        
        if (t == null) {
            throw new RuntimeException("No such node: "+pTgt);
        }
        
        // first, compute shortest path from t to each node, using Dijkstra        
        Timer.startTiming("Dijkstra preprocessing");
        Logger.info("Dijkstra preprocessing");
        Dijkstra.computeDistances(pGraph, t);        
        Timer.endTiming("Dijkstra preprocessing");
                
        Timer.startTiming("Sorting edges");
        Logger.info("Sorting edges");
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.sortEdgesAccordingToTargetDistance(false); // "best t-path first" (most robust?)                                 
        }
                                
        Timer.endTiming("Sorting edges");              
        
        Timer.startTiming("MC estimation");
        Logger.info("Actual MC estimation");
        
        // first, clear all node attributes, to be on the safe side:
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.clearAttributes();
        }        
        
        // bfs with a FIFO queue:
        LinkedList<TTNRNode<T>> queue = new LinkedList();                
        List<TTNRNode<T>> encounteredList = new ArrayList();
                
        int successCount = 0;
        int nTestedEdges = 0;
        int nFailedEdges = 0;
        int nWorkingEdges = 0;
                               
        boolean pathFound;        
                
        WeightedSet<Integer> distances = new HashWeightedSet<Integer>();
        
        for (int i=0; i<pNumIters; i++) {                                       
            pathFound = false;
            s.nEdgesDist = 0;
            t.nEdgesDist = -1;
                       
            if (s == t) {
                // a special case: a path from node to itself is always assumed to exist
                pathFound = true;                
            }
            else if (s.dist == Double.MAX_VALUE) {
                // not reachable (as found out by dijkstra)
                pathFound = false;
            }
            else {
                // the general case. 
                
                // first, clear the encountered flag of nodes encountered in the last round 
                for (TTNRNode<T> v: encounteredList) {
                    v.reached = false;
                }                
                encounteredList.clear();
                queue.clear();

                s.reached = true;
                encounteredList.add(s);
                queue.add(s);                
            
                while (!pathFound && queue.size() > 0) {
                    TTNRNode<T> u = queue.removeFirst();
                    List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                    int l = edges.size();
                    for (int j=0; j<l; j++) {
                        TTNREdge<T> e = edges.get(j);                                                        
                        TTNRNode<T> v = e.tgt;                            
                                                
                        if (!v.reached) {
                            // only have to test for existence of e if v 
                            // is not yet encountered; check it now:
                             
                            nTestedEdges++;                                                       
                            
                            if (myrand() >= e.attributes.prob_int) {
                                // edge failure
                                nFailedEdges++;                                
                            }
                            else {
                                // edge in working order
                                nWorkingEdges++;
                                v.reached = true;
                                v.nEdgesDist = u.nEdgesDist+1;
                                encounteredList.add(v);
                                queue.add(v);
                                if (v == t) {
                                    // Found t!
                                    pathFound = true;
                                    break;
                                }
                            }
                        }                                                       
                    }
                }                                              
            }
            
            if (pathFound) {
                 successCount++;
                 if (t.nEdgesDist == -1) {
                     throw new RuntimeException("WhatWhatWhat?!?!?!?");
                 }
                 distances.add(t.nEdgesDist);
            }

        }
        
        Logger.info("Number of tested edges:  "+nTestedEdges);
        Logger.info("Number of failed edges:  "+nFailedEdges);
        Logger.info("Number of working edges: "+nWorkingEdges);        
        
        Logger.info("Distribution of distances:\n"+distances);
        Logger.info("Number of distances:\n"+distances.countTotalWeight());
        Distribution<Integer> distr = new Distribution<Integer>(distances);
        double expectedDistance = distr.expectation();
        double reliability = ((double)successCount) / pNumIters;
        
        Timer.endTiming("MC estimation");
        // Timer.endTiming("computeTTNR_pairwise");
                
        ESPDResult result = new ESPDResult(reliability, expectedDistance, null);
        
        Logger.info("Got result: "+result);
        
        return result; 
    }
    
    /**
     * Compute expected path distance for a random graph. Note that 
     * there are two distinct weights for edges(!): the probability (attribute "prob")
     * and "length" (attribute len2). Compute the expected len according to the latter!
     * 
     * Disregard iterations where there is no path at all in the expectation computations
     * (store reliability as well!).
     */
    public static final <T> ESPDResult 
            expectedShortestPathDistance_weighted(TTNRGraph<T> pGraph,                                                               
                                                  T pSrc, T pTgt,
                                                  int pNumIters) {
       
        TTNRNode<T> src = pGraph.getNode(pSrc);
        TTNRNode<T> tgt = pGraph.getNode(pTgt);
        
        if (src == null) {
            throw new RuntimeException("No such node: "+pSrc);
        }
        
        if (tgt == null) {
            throw new RuntimeException("No such node: "+pTgt);
        }
        
        // first, compute shortest path from t to each node, using Dijkstra
        // (actually, most probable path!),
        // and sort edges according to this measure, in hope to improve performance
        Timer.startTiming("Dijkstra preprocessing");
        Logger.info("Dijkstra preprocessing");
        Dijkstra.computeDistances(pGraph, tgt);        
        Timer.endTiming("Dijkstra preprocessing");        
        
        // check if preliminary Dijkstra found any path:
        boolean noPathAtAll = (src.dist == Double.MAX_VALUE);
        
        Timer.startTiming("Sorting edges");
        Logger.info("Sorting edges");
        for (TTNRNode<T> u: pGraph.getNodes()) {
            u.sortEdgesAccordingToTargetDistance(false); // "best t-path first" (most robust?)                                 
        }
                                
        Timer.endTiming("Sorting edges");              
        
        Timer.startTiming("MC estimation");
        Logger.info("Actual MC estimation");
        
        // first, clear all node attributes and set dist to Double.MAX_VALUE
        // to signify no path has been found
        for (TTNRNode<T> u: pGraph.getNodes()) {
            u.clearAttributes();
            u.dist = Double.MAX_VALUE;
        }        
                
        // init heap for dijkstra
        BinaryHeap<TTNRNode, Double> heap = new BinaryHeap(BinaryHeap.Mode.MIN);                                               
        
        List<TTNRNode<T>> encounteredList = new ArrayList();
                
        int successCount = 0;
        int nTestedEdges = 0;
        int nFailedEdges = 0;
        int nWorkingEdges = 0;
                               
        boolean pathFound;             
                
        WeightedSet<Double> distances = new HashWeightedSet<Double>();
        
        for (int i=0; i<pNumIters; i++) {                                                   
                       
            if (src == tgt) {
                // disallow!
                throw new RuntimeException("Cannot handle case where s==t");                
            }
            else if (noPathAtAll) {
                // no s-t-paths in input (as found out by initial dijkstra)
                pathFound = false;
            }
            else {
                // the general case.
                pathFound = false;
                // upper bound == shortest found path at this iter so far
                double ub = Double.MAX_VALUE;
                
                // clear data for nodes encountered in the last round 
                for (TTNRNode<T> v: encounteredList) {
                    v.dist = Double.MAX_VALUE;                    
                }                
                encounteredList.clear();
                heap.clear();

                // put src to heap
                src.dist = 0.d;
                heap.add(src, 0.d);                
                encounteredList.add(src);
                       
                // Dijkstra main loop
                while (!heap.isEmpty() && heap.topKey() < ub) {
                    // shortest path not necessarily found yet
                    TTNRNode<T> u = heap.pop();
                    double d = u.getDist();
                    List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                    int l = edges.size();
                    for (int j=0; j<l; j++) {
                        TTNREdge<T> e = edges.get(j);                                                        
                        TTNRNode<T> v = e.tgt;
                        if (v == u) {
                            // TODO: remove this ASSERT
                            throw new RuntimeException("WhatWhatWhat?!?!?!?");
                        }
                    
                        double d2 = d+e.attributes.len2;
                        
                        if (d2 < v.dist) {
                            // found a new best path to v

                            // only have to test for existence of e if the 
                            // new path indeed is best (as is the case here):                            
                            nTestedEdges++;                                                       
                            
                            if (myrand() >= e.attributes.prob_int) {
                                // edge failure
                                nFailedEdges++;                                
                            }
                            else {
                                // edge in working order; actually found a new shortest path!
                                nWorkingEdges++;
                                if (v.dist == Double.MAX_VALUE) {
                                    // v not encountered yet in this iter
                                    heap.add(v, d2);
                                    encounteredList.add(v);
                                }
                                else {
                                    // v already in heap with a larger key, update now
                                    heap.updateKey(v, d2);
                                }
                                v.dist = d2;
                                                               
                                if (v == tgt) {
                                    // new best path to TARGET found!
                                    pathFound = true;
                                    ub = d2;
                                }                                                                                                                                                                                                                          
                            }
                        }                                                       
                    }
                }                                              
            }
            
            if (pathFound) {
                 successCount++;
                 if (tgt.dist == Double.MAX_VALUE) {
                     throw new RuntimeException("WhatWhatWhat?!?!?!?");
                 }
                 distances.add(tgt.dist);
            }

        }
        
        Logger.info("Number of tested edges:  "+nTestedEdges);
        Logger.info("Number of failed edges:  "+nFailedEdges);
        Logger.info("Number of working edges: "+nWorkingEdges);        
        
//        Logger.info("Distribution of distances:\n"+distances);
        Logger.info("Number of distances:\n"+distances.countTotalWeight());
        Distribution<Double> distr = new Distribution<Double>(distances);
        double expectedDistance = distr.expectation();
        double reliability = ((double)successCount) / pNumIters;
        
        Timer.endTiming("MC estimation");
        // Timer.endTiming("computeTTNR_pairwise");
                
        ESPDResult result = new ESPDResult(reliability, expectedDistance, distr);
        
        Logger.info("Got result: "+result);
        
        return result; 
    }
    
    
    
    
    
    
//    private class QueueNode<T> {
//        
//        TTNRNode<T> node;
//        int depth;
//        
//        QueueNode(TTNRNode<T> pNode, int pDepth) {
//            node = pNode;
//            depth = pDepth;
//        }
//        
//        
//    }
    
    
    /** Result of method {@link #expectedShortestPathDistance_numedges(TTNRGraph, Object, Object, int)}. */
    public static class ESPDResult {
        
        /** The "ordinary" reliability */
        public double reliability;
        
        /**
         * Distance, measured by number of links on shortest path.
         * Only iterations where there is any path are considered.
         */
        public double expectedDistance;
        public Distribution<Double> distanceDistribution;
        
        public ESPDResult(double pReliability,
                           double pExpectedDistance,
                           Distribution pDistr) {
            reliability = pReliability;
            expectedDistance = pExpectedDistance;
            distanceDistribution = pDistr;
        }
        
        public String toString() {
            return "reliability "+reliability+"\n"+
                   "expectedDistance "+expectedDistance;
        }
    }
    
    
    /**
     * Compute ttnr from pSrc to all other nodes of the network.
     * Store results as node attribute "ttnr".
     * 
     * TODO: do a multisource, which is just singlesource from many nodes,
     * so that all nodes get as a statistic the product of tge single-source
     * ttnr:s for over all query nodes. (already done in bmvis plugin!)
     * 
     * 
     */
    public static final <T> void computeTTNR_singlesource(TTNRGraph<T> pGraph,                                                               
                                                          T pSrc,                                                            
                                                          int pNumIters) {

        Timer.startTiming("computeTTNR_singlesource");
        
        TTNRNode<T> s = pGraph.getNode(pSrc);
        
        // first, clear all node attributes, to be on the safe side:
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.clearAttributes();
        }
        
        // then, compute length of shortest paths from st to each node as a reference
        Timer.startTiming("Dijkstra");
        Dijkstra.computeDistances(pGraph, s);        
        Timer.endTiming("Dijkstra");
        
        // Let's implement dfs with our own stack (without recursion)
        ArrayStack<TTNRNode<T>> stack = new ArrayStack();                
        List<TTNRNode<T>> encounteredList = new ArrayList();
                
        int nTestedEdges = 0;                              
        
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.reachedCount = 0;                       
        }
        
        s.reachedCount = pNumIters; // s is always reached
        
        for (int i=0; i<pNumIters; i++) {                                                  
            // first, clear the "reached" flag of nodes "reached" in the last round 
            for (TTNRNode<T> v: encounteredList) {
                v.reached = false;                
            }
            encounteredList.clear();
            stack.clear();

            s.reached = true;
            encounteredList.add(s);
            stack.push(s);
        
            while (stack.size() > 0) {
                TTNRNode<T> u = stack.pop();
                List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                int l = edges.size();
                for (int j=0; j<l; j++) {
                    TTNREdge<T> e = edges.get(j);                                                        
                    TTNRNode<T> v = e.tgt;                            
                                            
                    if (!v.reached) {
                        // only have to test existence of e if v was not yet reached:                         
                        nTestedEdges++;
                        
                        if (myrand() >= e.attributes.prob_int) {
                            // edge failure
                            continue;
                        }
                        else {
                            // edge exists
                            v.reached = true;
                            v.reachedCount++;
                            encounteredList.add(v);
                            stack.push(v);                            
                        }
                    }                                                       
                }
            }

        }
        
        // final clean up
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.reliability = ((double)v.reachedCount) / pNumIters;
            v.reached = false;
            v.reachedCount = 0;            
        }        
                        
        Logger.info("Number of tested edges: "+nTestedEdges);
                
        Timer.endTiming("computeTTNR_singlesource");        
    }          

    
    
    
    
//    /** Find reachable nodes, realizing edges on the fly */
//    private static Set<TTNRNode<T>> reachableNodes(TTNRNode<T> pStartingNode,
//                                                   Set) {
//        encountered.clear();
//        stack.clear();
//
//        encountered.add(s);
//        stack.push(s);
//    
//        while (!pathExists && stack.size() > 0) {
//            TTNRNode<T> u = stack.pop();
//            List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
//            int l = edges.size();
//            for (int j=0; j<l; j++) {
//                TTNREdge<T> e = edges.get(j);                                                        
//                TTNRNode<T> v = e.tgt;                            
//                
//                if (!(encountered.contains(v))) {
//                    // only have to test for existence of e if v 
//                    // is not yet encountered; therefore,
//                    // we only need to generate a new value of "exists" 
//                    // for this edge here, to be used in the next
//                    // round where this edge is found to be relevant
//                    
//                    boolean edgeExists = e.attributes.exists;                                   
//                    e.attributes.exists = myrand() < e.attributes.prob_int;
//                    
//                    if (!edgeExists) {                                    
//                        continue;
//                    }
//                    
//                    encountered.add(v);
//                    stack.push(v);
//                    if (v == t) {
//                        // Found t!
//                        pathExists = true;
//                        break;
//                    }
//                }                                                       
//            }
//
//    }
    
    /**
     * Here, we try make ttnr estimation more efficient by utilizing a 
     * heuristic: always expand nodes where the distance to t is smallest of the
     * so far encountered nodes.
     * 
     * For an graph with 300 nodes, 530 edges and reliability 0.9, we indeed
     * get the number of tested edges down from ~6M to ~2.5M (if we use
     * the inverse of the distance as a "heuristic" to make things as bad as
     * possible, we increase the number of tests to 13M (which is still less
     * than the theoretical maximum of 53M, were we to realize all edges
     * on each iteration).
     * 
     * Unfortunately, the additional costs overweight the benefits, and the 
     * running time is actually increased by almost 50%.
     * 
     * PERKELE: testattujen kaarten määrä ei vähene ttnr:ää heuristiikkana
     * käyttämällä, vaan käy päin vastoin! Kuten todettu muualla, ttnr
     * on lähes käyttökelvoton mitta tässä muodossaan.
     *
     */
    public static final <T> double computeTTNR_pairwise_bestpath_heuristic(TTNRGraph<T> pGraph,                                                               
                                                                           T pSrc,
                                                                           T pTgt,
                                                                           int pNumIters) {

        Timer.startTiming("computeTTNR_pairwise_bestpath_heuristic");
        
        TTNRNode<T> s = pGraph.getNode(pSrc);
        TTNRNode<T> t = pGraph.getNode(pTgt);
        
        // first, compute dijkstra distances to the nodes, starting from t
//        Timer.startTiming("Dijkstra");
//        Dijkstra.computeDistances(pGraph, t);
//        Timer.endTiming("Dijkstra");
        
        Timer.startTiming("Initial ttnr");
        Logger.info("Computing reliability to t for 1000 iterations");
        computeTTNR_singlesource(pGraph, pTgt, 1000);                
        Timer.endTiming("Initial ttnr");

               
        // Let's implement the traversal using a best path heuristic...
        // ArrayStack<TTNRNode<T>> stack = new ArrayStack();
        BinaryHeap<TTNRNode<T>, Double> queue = new BinaryHeap(BinaryHeap.Mode.MAX);
        List<TTNRNode<T>> encounteredList = new ArrayList();
                
        int successCount = 0; // total count of succesful iters                               
        int nTestedEdges = 0; 
            
        boolean pathFound; // path found in current iter?
        
        for (int i=0; i<pNumIters; i++) {                                       
            pathFound = false;
            
            if (s == t) {
                // let's define that there always is a path from a node to itself
                pathFound = true;
            }
//            else if (s.ttnr == 0.d) {
//                // not reachable
//                pathFound = false;
//            }                   
            else {
                // the general case. 
                
                // first, clear the encountered flag of nodes meeted in the last round 
                for (TTNRNode<T> v: encounteredList) {
                    v.reached = false;                    
                }
                encounteredList.clear();
                queue.clear();

                s.reached = true;                
                encounteredList.add(s);
//                queue.add(s, s.dist);
                queue.add(s, s.reliability);
            
                while (!pathFound && queue.size() > 0) {
                    TTNRNode<T> u = queue.pop();
                    List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                    int l = edges.size();
                    for (int j=0; j<l; j++) {
                        TTNREdge<T> e = edges.get(j);                                                        
                        TTNRNode<T> v = e.tgt;                            
                                                
                        if (!v.reached) {
                            // only have to test for existence of e if v 
                            // is not yet encountered; check it now:
                            
                            nTestedEdges ++;
                                                        
                            if (myrand() >= e.attributes.prob_int) {
                                // edge does not exist in this iter
                                continue;
                            }
                                                            
                            v.reached = true;
                            encounteredList.add(v);
                            // queue.add(v, v.dist);
                            
                            queue.add(v, v.reliability);
                            // Logger.info("Added to queue: "+v+": "+v.ttnr);
                            
                            if (v == t) {
                                // Found t!
                                pathFound = true;
                                break;
                            }
                        }                                                       
                    }
                }                                              
            }
            
            if (pathFound) {
                 successCount++;
            }

        }
                               
        Logger.info("Number of tested edges: "+nTestedEdges);
        
        Timer.endTiming("computeTTNR_pairwise_bestpath_heuristic");
        
        return ((double)successCount) / pNumIters;
              
    }
    
    /** 
     * Simply find connected components (starting from only nodes in pQueryNodes) 
     * For each connected component, only return the nodes in pQueryNodes
     * (one can find "traditional" connected components by giving null as 
     * pQueryNodes).
     * 
     * Todo: more generic impl (graph does not need to be tthrgraph!)
     * 
     * keywords: "connected components", "connectedcomponents"
     */
    public static <T> List<List<TTNRNode<T>>> connComps(TTNRGraph<T> pGraph,
                                                        Set<TTNRNode<T>> pQueryNodes) {                                        
        List<List<TTNRNode<T>>> result = new ArrayList();
        Set<TTNRNode<T>> unassignedNodes = new HashSet(); // nodes not yet in any component
        Collection<TTNRNode<T>> queryNodes = pQueryNodes != null
                                           ? pQueryNodes
                                           : pGraph.getNodes();
        
        unassignedNodes.addAll(queryNodes);
        Set<TTNRNode<T>> comp = new HashSet();             // nodes in the current connected component
        ArrayStack<TTNRNode<T>> stack = new ArrayStack(); // stack for dfs                                                                                                                                            
        
        while (unassignedNodes.size() != 0) {            
            // find one connected component in each execution of this while block            
            comp.clear();                
            stack.clear();
                            
            TTNRNode<T> s = unassignedNodes.iterator().next();                                                                                                             
            comp.add(s);
            stack.push(s);                
        
            // traverse connected component using dfs 
            while (stack.size() > 0) {
                TTNRNode<T> u = stack.pop();
                List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                int l = edges.size();
                for (int j=0; j<l; j++) {
                    TTNREdge<T> e = edges.get(j);                                                        
                    TTNRNode<T> v = e.tgt;                            
                    
                    if (!(comp.contains(v))) {
                        // not yet encountered                    
                        comp.add(v);
                        stack.push(v);
                    }
                                                                         
                }
            }
            
            // finished constructing a single connected component
            Set<TTNRNode<T>> queryNodesInComp = new HashSet();
            
            for (TTNRNode<T> qn: unassignedNodes) {
                if (comp.contains(qn)) {
                    queryNodesInComp.add(qn);                        
                }                               
            }
            
            unassignedNodes.removeAll(queryNodesInComp);
            
            ArrayList nodeList = new ArrayList(queryNodesInComp);
            result.add(nodeList);
        }
        
        return result;
        
    }
    
    /**
     * Perform a number of random realizations of a graph. For each realization,
     * output sets of query nodes that end up in the same realization. 
     */
    public static <T> void recordConnComps(TTNRGraph<T> pGraph,
                                           Set<TTNRNode<T>> pQueryNodes,
                                           int pNumIters) {
                       
        realizeEdges(pGraph.getEdges());
                
        
        Set<TTNRNode<T>> unassignedNodes = new HashSet(); // nodes not yet in any component 
        Set<TTNRNode<T>> comp= new HashSet();             // nodes in the current connected component
        ArrayStack<TTNRNode<T>> stack = new ArrayStack(); // stack for dfs
        
        for (int i=0; i<pNumIters; i++) {
            if (i % 1000 == 0) {
                Logger.info("Iter: "+i);
            }
                         
            unassignedNodes.clear();            
            unassignedNodes.addAll(pQueryNodes);            
            
                                     
            int compInd = 0;
            
            List<Integer> connCompSizes = new ArrayList();
            
            while (unassignedNodes.size() != 0) {
                
                // find one connected component in each execution of this while block
                compInd++;
                comp.clear();                
                stack.clear();
                                
//                TTNRNode<T> s = RandUtils.sampleFromCollection(unassignedNodes, 1).iterator().next();
                 TTNRNode<T> s = unassignedNodes.iterator().next();                                                                                                             
                comp.add(s);
                stack.push(s);                
            
                // traverse connected component using dfs 
                while (stack.size() > 0) {
                    TTNRNode<T> u = stack.pop();
                    List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                    int l = edges.size();
                    for (int j=0; j<l; j++) {
                        TTNREdge<T> e = edges.get(j);                                                        
                        TTNRNode<T> v = e.tgt;                            
                        
                        if (!(comp.contains(v))) {
                            // only have to test for existence of e if v 
                            // is not yet encountered
                            
                            boolean exists = e.attributes.exists;
                            // randomize e for the next round:
                            e.attributes.exists = myrand() < e.attributes.prob_int;
                            
                            if (!exists) {
                                // edge does not exist in this realization => move on to the next edge
                                continue;
                            }
                            else {
                                // edge exists in this realization
                                comp.add(v);
                                stack.push(v);
                            }
                        }                                                       
                    }
                }
                
                // finished constructing a single connected component
                // TODO: more efficient intersection?
                Set<TTNRNode<T>> queryNodesInComp = new HashSet();
                
                for (TTNRNode<T> qn: unassignedNodes) {
                    if (comp.contains(qn)) {
                        queryNodesInComp.add(qn);                        
                    }                               
                }
                
                unassignedNodes.removeAll(queryNodesInComp);
                
                ArrayList nodeList = new ArrayList(queryNodesInComp);
                Collections.sort(nodeList, new ByStringComparator());
                System.out.println("SINGLE_COMP iter "+i+" conn comp "+compInd+": "+StringUtils.listToString(nodeList, " "));
                
                connCompSizes.add(queryNodesInComp.size());                
            }
            
            Collections.sort(connCompSizes, new ReverseComparator());
            System.out.println("COMP_SIZES iter "+i+" " +StringUtils.listToString(connCompSizes, " "));
        }
        
        
                           
//        Logger.info("Returning ttnrs:\n"+
//                     result.toString());
                
                      
    }
    
    
    
    
    /**
     * Computes ttnr for all pairs of query nodes in a set S.
     * 
     * Uses a brute-force method based on finding connected components (assumes
     * "many" query nodes to make sense).
     */
    public static <T> WeightedSet<UnorderedPair<TTNRNode<T>>> computeTTNR_allpairs_conncomps(TTNRGraph<T> pGraph,
                                                                                             Set<TTNRNode<T>> pQueryNodes,
                                                                                             int pNumIters) {
        
        HashMultiSet<UnorderedPair<TTNRNode<T>>> successCounts = new HashMultiSet(); 
        
        realizeEdges(pGraph.getEdges());
        
        Set<TTNRNode<T>> unassignedNodes = new LinkedHashSet<TTNRNode<T>>(); // nodes not yet in any component 
        Set<TTNRNode<T>> comp= new HashSet();             // nodes in the current connected component
        ArrayStack<TTNRNode<T>> stack = new ArrayStack(); // stack for dfs
        
        for (int i=0; i<pNumIters; i++) {
            if (i % 1000 == 0) {
                Logger.info("Iter: "+i);
            }
                         
            unassignedNodes.clear();
            unassignedNodes.addAll(pQueryNodes);
                                     
            while (unassignedNodes.size() != 0) {
                // find one connected component in each execution of this while block
                comp.clear();                
                stack.clear();
                
                TTNRNode<T> s = unassignedNodes.iterator().next();                                                                                                             
                comp.add(s);
                stack.push(s);                
            
                while (stack.size() > 0) {
                    TTNRNode<T> u = stack.pop();
                    List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                    int l = edges.size();
                    for (int j=0; j<l; j++) {
                        TTNREdge<T> e = edges.get(j);                                                        
                        TTNRNode<T> v = e.tgt;                            
                        
                        if (!(comp.contains(v))) {
                            // only have to test for existence of e if v 
                            // is not yet encountered
                            
                            boolean exists = e.attributes.exists;
                            // randomize e for the next round:
                            e.attributes.exists = myrand() < e.attributes.prob_int;
                            
                            if (!exists) {
                                // edge does not exist in this realization => move on to the next edge
                                continue;
                            }
                            else {
                                // edge exists in this realization
                                comp.add(v);
                                stack.push(v);
                            }
                        }                                                       
                    }
                }
                
                // finished constructing a single connected component
                // TODO: more efficient intersection?
                Set<TTNRNode<T>> queryNodesInComp = new HashSet();
                
                for (TTNRNode<T> qn: unassignedNodes) {
                    if (comp.contains(qn)) {
                        queryNodesInComp.add(qn);
                    }
                }
                    
                List<UnorderedPair<TTNRNode<T>>> linkedPairs = CollectionUtils.makeUnorderedPairs(queryNodesInComp);
                                                                      
                for (UnorderedPair<TTNRNode<T>> linkedPair: linkedPairs) {
                    successCounts.add(linkedPair);
                }
                
                unassignedNodes.removeAll(queryNodesInComp);
            }
        }
        
        WeightedSet<UnorderedPair<TTNRNode<T>>> result = successCounts.normalizedVersion(pNumIters); 
                           
//        Logger.info("Returning ttnrs:\n"+
//                     result.toString());
        
        return result;
                      
    }
        
    
    /**
     * Test, by simulation, how probable it is that exists at least one 
     * spanning tree connecting all elements of pTerminals in a realization
     * of pGraph.
     * 
     * TODO: optimize by starting from the node with the greatest betweenness
     * (of closeness?) centrality, with respect to pTerminals.
     * 
     * Compute reachedcount to each v as a side effect; v is "reached"
     * is it is in the same component with all terminals.
     * 
     * OK, let's map reachedcount to ktnr; for starting nodes,
     * it is of course exactly the ktnr; for other nodes,
     * it is the prob of belonging into the same component with all terminals.
     * Store the ktnr to the field "reliability"
     */ 
    public static final <T> double ktnr(TTNRGraph<T> pGraph,                                                               
                                        Set<T> pTerminals,                                       
                                        int pNumIters,
                                        boolean pComputeKtnrForIntermediateNodes) {

        Logger.info("Starting TTNRUtils.ktnr(), terminals: "+
                    pTerminals);
        
        // first, clear all node attributes, to be on the safe side:
        for (TTNRNode<T> v: pGraph.getNodes()) {
            v.clearAttributes();
        }        
        
        Set<TTNRNode> terminals = new HashSet(pTerminals.size());
        
        // set query node flag to terminals
        for (T s_key: pTerminals) {
            TTNRNode<T> s = pGraph.getNode(s_key);
            s.isQueryNode = true;            
            terminals.add(s);
        }
            
        // always start from the same terminal
        TTNRNode<T> s = terminals.iterator().next();
        Logger.info("Starting search from terminal: "+s);
        
        // todo: should we compute connected components in the original graph
        // once, to prevent some hopeless crawling?
        
        Timer.startTiming("MC estimation");
        Logger.info("Actual MC estimation");
                       
        ArrayStack<TTNRNode<T>> stack = new ArrayStack(); // for dfs
        List<TTNRNode<T>> comp = new ArrayList();             // nodes in the current connected component       
                
        int successCount = 0;
        int nTestedEdges = 0;
        int nFailedEdges = 0;
        int nWorkingEdges = 0;
                  
        int nTerminals = terminals.size();
        int nFound;        
        
        for (int i=0; i<pNumIters; i++) {                                                                                                  
            if (i % 1000 == 0) {
                Logger.info("Iter: "+i);
            }
            
            nFound = 0;
            
            for (TTNRNode<T> v: comp) {
                v.reached = false;                
            }

            // All there really is to do is to find a connected component 
            // starting from an arbitrary terminal; if the comp contains all 
            // terminals, we have a spanning tree otherwise we do not
            
            comp.clear();                
            stack.clear();
                                                                                                                                     
            comp.add(s);
            stack.push(s);                
            s.reached = true;
            nFound++; // always "find" s 
                    
            while ((pComputeKtnrForIntermediateNodes || (nFound < nTerminals))
                    && !(stack.isEmpty())) {
                TTNRNode<T> u = stack.pop();
                List<TTNREdge<T>> edges= pGraph.getEdges(u);                        
                int l = edges.size();
                for (int j=0; j<l; j++) {
                    TTNREdge<T> e = edges.get(j);                                                        
                    TTNRNode<T> v = e.tgt;                            
                    
                    if (!v.reached) {
                        // only have to test for existence of e if v 
                        // is not yet encountered
                        nTestedEdges++;
                        
                        if (myrand() >= e.attributes.prob_int) {
                            // edge failure in this realization
                            nFailedEdges++;                            
                        }
                        else {                                                                                        
                            // edge exists in this realization
                            nWorkingEdges++;
                            comp.add(v);
                            stack.push(v);
                            v.reached = true;
                            if (v.isQueryNode) {
                                nFound++;
                            }
                        }
                    }                                                       
                }
            }
            
//            Logger.info("nfound: "+nFound);
            
            // finished constructing one connected component, see if we reached all terminals
            if (nFound > nTerminals) {
                throw new RuntimeException("???");
            }
            if (nFound == nTerminals) {
                // a spanning tree was found
                successCount++;
                
                // update reached counts
                if (pComputeKtnrForIntermediateNodes) {
                    for (TTNRNode<T> v: comp) {
                        v.reachedCount++;
                    }                
                }
            }        
        }                                                                          
    
        Logger.info("Number of tested edges:  "+nTestedEdges);
        Logger.info("Number of failed edges:  "+nFailedEdges);
        Logger.info("Number of working edges: "+nWorkingEdges);
        
        Timer.endTiming("MC estimation");        
        
         // final wrap up
        for (TTNRNode<T> v: pGraph.getNodes()) {
            if (pComputeKtnrForIntermediateNodes) {
                v.reliability = ((double)v.reachedCount) / pNumIters;
            }
            v.reached = false;
            v.reachedCount = 0;            
        }
        
        return ((double)successCount) / pNumIters;
              
    }





}
