package util.collections.graph.defaultimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import util.collections.BinaryHeap;
import util.collections.Pair;
import util.dbg.Logger;
import util.factory.Factory;

public class Dijkstra {

    /**
     * Find the shortest path from pSrc to pTgt, using Dijkstra
     * TODO: Note that the current impl adds all nodes to the heap, 
     * and may not be optimal because of that!!!
     * 
     * A better way would be to only put encountered nodes to the heap...
     * 
     * Note that self-links (that is, links of the form {n,n]} will crash this!
     * 
     * @param pMaxEdges max number of edges on a path. Null for no limit.
     * Also consider 0 as null for convenience.
     * Note that the implementation cheats a bit; that is, npaths is naively 
     * stored for only for the best path in each node, instead of considering 
     * the best path for all path lengths separately. This of course means
     * that we could miss a valid path when it is so that some partial path p1 to 
     * a node, say v, is better than a partial path p2, but has more edges;
     * then, it might not be possible to extend p1 further (due to the numedges limit)
     * to get a complete path, while it might still be possible to extend p2.
     * TODO: actually, a correct impl would be nice, and this would probably 
     * have to be a separate method alltogether, for the sake of clarity.  
     * 
     * @param maxInsteadOfSum if true, path len defined as the length of the longest edge
     * on the path, instead of sum of lengths
     * 
     * @pResult a path of length 0, where the results shall be put. If null,
     *          a new DefaultPath shall be constructed. 
     */
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge,
                   P extends DefaultPath<T,N,E>>
        Pair<P, Double> shortestPath(DefaultGraph<T,N,E> pGraph, 
                                     N pSrc,
                                     N pTgt,
                                     P pResult,                                     
                                     Integer pMaxEdges,
                                     boolean pMaxInsteadOfSum) {
        
        int maxEdges = (pMaxEdges != null ? pMaxEdges : 0);  
                                                                             
        Logger.dbg("Starting dijkstra search from "+pSrc+" to "+pTgt);
         
        BinaryHeap<N, Double> heap = new BinaryHeap(BinaryHeap.Mode.MIN);
        // init distances to all graph nodes as "infinite" 
        for (N n: pGraph.getNodes()) {
            // todo: how to avoid duplicate maintenance of distance (both in heap
            // and in the node itself...)
            // TODO: why is everything added to the heap?!?!?!
            n.setDist(Double.MAX_VALUE);
            heap.add(n, Double.MAX_VALUE);
            if (maxEdges != 0) {
                n.setNEdgesDist(Integer.MAX_VALUE);
            }
        }
        double ub = Double.MAX_VALUE;                
        
        pSrc.setDist(0.d);
        heap.updateKey(pSrc, 0.d);
        if (maxEdges != 0) {
            pSrc.setNEdgesDist(0);
        }
        
        while (!heap.isEmpty() && heap.topKey() < ub) {
            // shortest path not necessarily found yet
            N s = heap.pop();
            if (maxEdges != 0 && s.getNEdgesDist() >= pMaxEdges) {
                // max path len reached, do not continue path from this node
                continue;
            }
            double d = s.getDist();
            for (E e: pGraph.getEdges(s)) {
                N t = e.getOtherNode(s);
                                
                // NEW:
                double d2;
                if (pMaxInsteadOfSum) {
                    d2 = Math.max(d, e.getLen());
                }
                else {
                    d2 = d+e.getLen();
                }
                
                if (d2 < t.getDist()) {
                    // found a new best path to t
                    t.setDist(d2);
                    t.setParentLink(e.startingFrom(t));
                    heap.updateKey(t, d2);
                    if (maxEdges != 0) {
                        t.setNEdgesDist(s.getNEdgesDist()+1);
                    }
                    if (t == pTgt) {
                        // new best path to target found
                        ub = d2;
                    }
                }                                     
            }
        }
        
        if (ub != Double.MAX_VALUE) {
            // a path was found
            List<E> edgeList = new ArrayList();            
            
            N n = pTgt;
            while (n != pSrc) {
                E e = (E)n.getParentLink();                
                n = e.getOtherNode(n);
                // add edge in the "s=>t" direction: 
                edgeList.add((E)e.startingFrom(n));
            }
            Collections.reverse(edgeList);
            
            P result = pResult != null ? pResult : (P)new DefaultPath();
            result.addAll(edgeList);
            
            return new Pair(result, ub);
        }        
        else {
            // no path
            return new Pair(null, ub);
        }
    }
    
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge,
                   P extends DefaultPath<T,N,E>>
            Pair<P, Double> shortestPath(DefaultGraph<T,N,E> pGraph, 
                                     N pSrc,
                                     N pTgt,
                                     P pResult,                                     
                                     Integer pMaxEdges) {
        return shortestPath(pGraph, pSrc, pTgt, pResult, pMaxEdges, false); 
    }
                                     
    
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge,
                   P extends DefaultPath<T,N,E>>
            Double shortestPathLenExcludingDirectEdge(DefaultGraph<T,N,E> pGraph, 
                                                      N pSrc,
                                                      N pTgt) {
        return shortestPathLenExcludingDirectEdge(pGraph, pSrc, pTgt, false);
    }
    

    /**
     * Find the shortest path from pSrc to pTgt that DOES NOT use any direct link 
     * between s and t!
     * 
     * This is copy-pasted from method shortestPath()...
     * 
     * @param maxInsteadOfSum if true, path len defined as the length of the longest edge
     * on the path, instead of sum of lengths
     * 
     * @return length of shortest path, or null if no path found...
     */
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge,
                   P extends DefaultPath<T,N,E>>
        Double shortestPathLenExcludingDirectEdge(DefaultGraph<T,N,E> pGraph, 
                                                  N pSrc,
                                                  N pTgt,
                                                  boolean pMaxInsteadOfSum) {         
        
        Logger.dbg("Starting dijkstra search from "+pSrc+" to "+pTgt);
         
        BinaryHeap<N, Double> heap = new BinaryHeap(BinaryHeap.Mode.MIN);
        // init distances to all graph nodes as "infinite" 
        for (N n: pGraph.getNodes()) {
            // todo: how to avoid duplicate maintenance of distance (both in heap
            // and in the node itself...)
            n.setDist(Double.MAX_VALUE);
            heap.add(n, Double.MAX_VALUE);            
        }
        double ub = Double.MAX_VALUE;                
        
        pSrc.setDist(0.d);
        heap.updateKey(pSrc, 0.d);        
        
        while (!heap.isEmpty() && heap.topKey() < ub) {
            // shortest path not necessarily found yet
            N s = heap.pop();
            
            double d = s.getDist();
            for (E e: pGraph.getEdges(s)) {                
                N t = e.getOtherNode(s);
                if (s == pSrc && t == pTgt) {
                    // THIS EDGE IS TO BE IGNORED!
                    continue;
                }                
                
                // OLD:
                // double d2 = d+e.getLen();
                
                // NEW:
                double d2;
                if (pMaxInsteadOfSum) {
                    d2 = Math.max(d, e.getLen());
                }
                else {
                    d2 = d+e.getLen();
                }
                
                if (d2 < t.getDist()) {
                    // found a new best path to t
                    t.setDist(d2);
                    t.setParentLink(e.startingFrom(t));
                    heap.updateKey(t, d2);
                    
                    if (t == pTgt) {
                        // new best path to target found
                        ub = d2;
                    }
                }                                     
            }
        }
        
        if (ub != Double.MAX_VALUE) {
            // a path was found
            List<E> edgeList = new ArrayList();            
            
            N n = pTgt;
            while (n != pSrc) {
                E e = (E)n.getParentLink();                
                n = e.getOtherNode(n);
                // add edge in the "s=>t" direction: 
                edgeList.add((E)e.startingFrom(n));
            }
            Collections.reverse(edgeList);
                                   
            return ub;
        }        
        else {
            // no path
            return null;
        }
    }
    
    
    /** 
     * It is very probably better that S is the smaller one of the given query
     * node sets.
     * 
     * @param maxInsteadOfSum if true, path len defined as the length of the longest edge
     * on the path, instead of sum of lengths 
     */
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge,
                   P extends DefaultPath<T,N,E>>
        List<Pair<P, Double>> shortestPaths_allPairs(DefaultGraph<T,N, E> pGraph,
                                                     Set<N> pSrcNodes,
                                                     Set<N> pTargetNodes,
                                                     Factory<P> pPathFactory,
                                                     boolean maxInsteadOfSum) {
        ArrayList<Pair<P, Double>> result = new ArrayList();
        
        Set<N> handledSourceNodes = new HashSet();            

        for (N s: pSrcNodes) {
            Set<N> T = new LinkedHashSet(pTargetNodes);
            T.remove(s);
            T.removeAll(handledSourceNodes);
            if (T.size() > 0) {
                result.addAll(shortestPaths_singlesource(pGraph, s, T, pPathFactory, maxInsteadOfSum));
            }
            handledSourceNodes.add(s);
        }
        
        return result;
    }
    
    /**
     * Find the shortest path from pSrc to pTgt, using Dijkstra
     * TODO: Note that the current impl adds all nodes to the heap, 
     * and may not be optimal because of that!!!
     * 
     * A better way would be to only put encountered nodes to the heap...
     * 
     * Note that self-links (that is, links of the form {n,n]} will crash this!
     * 
     * @param maxInsteadOfSum if true, path len defined as the length of the longest edge
     * on the path, instead of sum of lengths
     * 
     * @pResult a path of length 0, where the results shall be put. If null,
     *          a new DefaultPath shall be constructed. 
     */
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge,
                   P extends DefaultPath<T,N,E>>
        List<Pair<P, Double>> shortestPaths_singlesource(DefaultGraph<T,N, E> pGraph,
                                                         N pSrcNode,
                                                         Set<N> pTargetNodes,
                                                         Factory<P> pPathFactory,
                                                         boolean maxInsteadOfSum) {                                                                                                                                      
         
        BinaryHeap<N, Double> heap = new BinaryHeap(BinaryHeap.Mode.MIN);
        BinaryHeap<N, Double> targetNodesHeap = new BinaryHeap(BinaryHeap.Mode.MAX);        
        
        for (N n: pGraph.getNodes()) {
            // todo: how to avoid duplicate maintenance of distance (both in heap
            // and in the node itself...)
            n.setDist(Double.MAX_VALUE);
            heap.add(n, Double.MAX_VALUE);            
        }
        
        for (N n: pTargetNodes) {
//            Logger.info("Adding target node to heap: "+n);
            targetNodesHeap.add(n, Double.MAX_VALUE);
        }
            
        pSrcNode.setDist(0.d);
        heap.updateKey(pSrcNode, 0.d);       
        
        while (!heap.isEmpty() && heap.topKey() < targetNodesHeap.topKey()) {
            // shortest path necessarily not found yet
            N s = heap.pop();
            double d = s.getDist();
            for (E e: pGraph.getEdges(s)) {
                N t = e.getOtherNode(s);
                double d2;
                if (maxInsteadOfSum) {
                    d2 = Math.max(d, e.getLen());
                }
                else {
                    d2 = d+e.getLen();
                }
//                double d2 = Math.max(d, e.getLen()); 
                if (d2 < t.getDist()) {
                    // found a new best path to t
                    t.setDist(d2);
                    t.setParentLink(e.startingFrom(t));
                    heap.updateKey(t, d2);
                    if (pTargetNodes.contains(t)) {
                        // new best path to target found
                        targetNodesHeap.updateKey(t, d2);
                    }
                }                                     
            }
        }
        
        ArrayList<Pair<P, Double>> result = new ArrayList();
        
        for (N t: pTargetNodes) {        
            double d = t.getDist();
            if (d != Double.MAX_VALUE) {
                // a path to t was found
                List<E> edgeList = new ArrayList();            
                
                N n = t;
                while (n != pSrcNode) {
                    E e = (E)n.getParentLink();                
                    n = e.getOtherNode(n);
                    // add edge in the "s=>t" direction: 
                    edgeList.add((E)e.startingFrom(n));
                }
                Collections.reverse(edgeList);
                
                P path = pPathFactory != null
                        ? pPathFactory.makeObject()
                        : (P)new DefaultPath();
                path.addAll(edgeList);
                
                result.add(new Pair(path, d));
            }
        }
        
        return result;
    }
    
    
    
    /**
     * Compute the distance from pSrc to all other nodes of the graph;
     * store the distances to attribute dist of the reached nodes.
     * 
     * If a node is not reached, it's dist shall be set to Double.MAX_VALUE.
     * 
     * Apologies for the horrible type parameter lists.
     * 
     * NOTE: this is currently the "best" method of writing signatures
     * here. Actually, we should have some "INode" and "IEdge", and 
     * have IDijkstraNode and IDijkstraEdge implement those...
     * Also, should have AbstractNode&Edge, so that everything
     * would not inherit DefaultNode&Edge. 
     */
    public static <T,
                   N extends DefaultNode<T> & IDijkstraNode,
                   E extends AbstractEdge<T,N> & IDijkstraEdge>                     
        void computeDistances(DefaultGraph<T,N, E> pGraph, N pSrc) {
                                                                                                                                       
        Logger.dbg("Starting dijkstra search from "+pSrc);
         
        BinaryHeap<N,Double> heap = new BinaryHeap(BinaryHeap.Mode.MIN);

        for (N n: pGraph.getNodes()) {
            // todo: how to avoid duplicate maintenance of distance (both in heap
            // and in the node itself...)
            n.setDist(Double.MAX_VALUE);
            heap.add(n, Double.MAX_VALUE);
        }
                       
        pSrc.setDist(0.d);
        heap.updateKey(pSrc, 0.d);
        
        while (!heap.isEmpty()) {            
            N u = heap.pop();
            double d = u.getDist();
            for (E e: pGraph.getEdges(u)) {
                N v = e.tgt;                
                double d2 = d+e.getLen();
                if (d2 < v.getDist()) {
                    // found a new best path to v
                    v.setDist(d2);                    
                    heap.updateKey(v, d2);                                        
                }                                     
            }
        }
    }
                            
    
    /** args: <file> <srcnode> <tgtnode> */
    public static void main(String[] args) throws Exception {
        
        // CmdLineArgs args = new CmdLineArgs(pArgs); 
        
        String file = args[0];
        String srcId = args[1];
        String tgtId = args[2];
        
        SimpleDijkstraGraph g = new SimpleDijkstraGraph(file);
        
//        g.convertWeights(new Converter<Double,Double>() {
//            public Double convert(Double p) {
//                return (double)-Math.log(p.doubleValue());
//            }
//        });
        
        DijkstraNode<String> src = g.getNode(srcId);
        DijkstraNode<String> tgt = g.getNode(tgtId);
                       
        Pair<DefaultPath, Double> result = 
            (Pair<DefaultPath, Double>)(Object)shortestPath(g, src, tgt, null, null);
        
        Logger.info("Path: "+result.getObj1());
        Logger.info("Goodness: "+Math.exp(-result.getObj2()));
         
        // System.out.println(g);
    }
}
