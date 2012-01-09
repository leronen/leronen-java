package util.collections.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.IOUtils;
import util.collections.MultiMap;


/** 
 * Filosofiaa: mikäli mahdollista, lähes kaikki logiikka pitäisi laittaa 
 * graafiluokan huoleksi; solmuissa ja kaarissa ei pitäisi oikeastaan 
 * olla mitään operaatioita. Tämä siksi, että olemassaolevia rakenteita 
 * on huomattavasti helpompi wrapata toteuttamalla pelkkä graafirajapinta,
 * kuin toteuttamalla kaariin ja solmuihin rajapinta. Joskus kaaria ei edes
 * ole olemassa eksplisiittisinä olioina. Sallittakoon kuitenkin kutsujan
 * viitata solmuihin (ja kaariin(?); tällöin kaarille pitäisi mahd. jotenkin 
 * lennossa luoda instansseja...) Solmujen ja kaarten tyypit voisi 
 * olla vaikka parametrisoitu (vai tarvitaanko joskus kuitenkin 
 * jotain interfaceja?) Riittääkö edgen esitykseksi SymmetricPair<Node>
 * tai vastaava?
 */ 
public class GraphUtils {
    
    /**
     * @param pIncludeStartingNodes if false, the result shall not automatically 
     * include the starting node; however, if the starting node
     * is reached by a cycle starting from the starting node itself,
     * it will be included in the result.
     * 
     */
    public static <T> Set<T>reachableNodes(IGraph<T> pGraph,
                                           T pStartingNode,
                                           boolean pIncludeStartingNode) {
        return reachableNodes(pGraph, Collections.singleton(pStartingNode), pIncludeStartingNode);
    }
    
    /**
     * @param pIncludeStartingNodes if false, the result shall not automatically 
     * include  any of the starting nodes; however, if a starting node
     * is reached by any path with len>0 (including a cycle starting from the
     * node itself), it will be included in the result. 
     */
    public static <T> Set<T>reachableNodes(IGraph<T> pGraph,
                                           Set<T> pStartingNodes,
                                           boolean pAlwaysIncludeStartingNodes) {
        Set<T> result = new HashSet();
        LinkedList<T> stack = new LinkedList<T>();
        // note that we always add starting nodes to result,
        // to avoid expanding them multiple times
        // if pIncludeStartingNodes is false, we shall later remove them...
        if (pAlwaysIncludeStartingNodes) {
            result.addAll(pStartingNodes);
        }
        stack.addAll(pStartingNodes);
        while (stack.size()>0) {
            T n = stack.pop();
            Iterable<T> followers = pGraph.followers(n);
            for (T f: followers) {
                if (!(result.contains(f))) {
                    result.add(f);
                    stack.push(f);
                }
            }
        }               
        
        return result;        
    }
    
    /**
     * Find a path between a given pair of nodes, if one exists. 
     * Maybe not so efficient, as there is no heuristic to guide the search,
     * and the search is one-directional.
     * 
     * @return the path as a list: (s,...,t), null if no path exists, or empty 
     *         list if s and t are the same node. 
     */
    public static <T> List<T> findPath_bfs(IGraph<T> pGraph,
                                           T pSourceNode,
                                           T pTargetNode) {
        if (pSourceNode.equals(pTargetNode)) {
            // a special case
            return Collections.EMPTY_LIST;
        }
        
        Map<T, T> parentLinks = new HashMap();
        
        Set<T> encountered = new HashSet();
        
        LinkedList<T> queue = new LinkedList<T>();        
        
        encountered.add(pSourceNode);
        queue.add(pSourceNode);        
        
        while (queue.size() > 0) {
            T u = queue.removeFirst();
            Iterable<T> followers = pGraph.followers(u);
            for (T v: followers) {
                if (!(encountered.contains(v))) {
                    encountered.add(v);
                    parentLinks.put(v, u);
                    queue.push(v);
                }
                
                if (v.equals(pTargetNode)) {
                    // Found it!
                    // construct a list s -> u -> v -> . . . > t
                    // and return it
                    List<T> result = new ArrayList();
                    result.add(v);
                    T w = parentLinks.get(v);
                    while (!(w.equals(pSourceNode))) {
                        result.add(w);
                        w = parentLinks.get(w);
                    }
                    result.add(pSourceNode);
                    Collections.reverse(result);
                    return result;
                }
            }
        }
        
        // no path!
        return null;
                     
    }
        
    
    public static void main(String[] args) throws Exception {
        
        MultiMap<String, String> data = new MultiMap();
        
        String s = args[0];
        String t = args[1];
        
        for (String line: IOUtils.readLines(System.in)) {
            String[] tokens = line.split("\\s+");
            String u = tokens[0];
            String v = tokens[1];
            data.put(u,v);
            data.put(v,u);
        }
        
        List<String> path = GraphUtils.findPath_bfs(data.asDirectedGraph(), s, t);
        System.out.println("Path: "+path);
                                                    
        
        
    }
}
