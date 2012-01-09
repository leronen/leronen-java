package util.collections.graph.defaultimpl;

import java.util.*;

import util.dbg.Logger;
/**
 * Let's initially use this only for util.collections.graph.defaultimpl.Graph 
 * implementations (admittably, there is a clear conflict of intrests
 * with util.collections.graph.GraphUtils, which is parametrized only 
 * by the type of node, and does not allow for any edge attributes,
 * (and does not actually know anything about edges). 
 * 
 * Here, we shall parametrize everything according to the node and edge types,
 * and always use a Graph<T, N extends Node<T>, E extends Edge<T,N>>
 * (later, we could perhaps abstract that into a 
 * IGraph<T, N extends Node<T>, E extends Edge<T,N>>?
 * 
 * Furthermore, let's use this so that the user first constructs a GraphAlgorithms 
 * object, and then calls instance methods of this object. This is done to
 * get rid of tedious parameterizations for each individual method.
 * 
 * So, the parametrization is the same as in class util.collection.graph.defaultimpl.Graph.
 */ 
public class GraphAlgorithms<T, N extends DefaultNode<T>, E extends DefaultEdge<T,N>> {
    
    /** Always includes the starting nodes in the result */
    public Set<N> reachableNodes_depthfirst(DefaultGraph<T,N,E> pGraph,
                                            Set<N> pStartingNodes) {
        Set<N> result = new HashSet();
        LinkedList<N> stack = new LinkedList();
        // note that we always add starting nodes to result,
        // to avoid expanding them multiple times
        // if pIncludeStartingNodes is false, we shall later remove them...        
        result.addAll(pStartingNodes);            
        stack.addAll(pStartingNodes);
        
        while (stack.size()>0) {
            N n = stack.pop();
            Iterable<E> edges = pGraph.getEdges(n);
            for (E e: edges) {
                N n2 = e.getOtherNode(n);                
                if (!(result.contains(n2))) {
                    result.add(n2);
                    stack.push(n2);
                }
            }
        }
        
        return result;
    }
    
    public List<Set<N>> connectedComponents(DefaultGraph<T,N,E> pGraph) {                                             
        Logger.info("Starting to split a graph with " +pGraph.numNodes()+" nodes to connected components.");
        List<Set<N>> result = new ArrayList();
        Set<N> unassignedNodes = new HashSet(pGraph.getNodes());
        
        while (unassignedNodes.size() != 0) {
            N initialNode = unassignedNodes.iterator().next();
            Set<N> nodesInThisComponent = reachableNodes_depthfirst(pGraph, Collections.singleton(initialNode));                               
            result.add(nodesInThisComponent);
            unassignedNodes.removeAll(nodesInThisComponent);
        }

        Logger.info("Graph has been split to " + result.size()+ " connected components.");
                  

        return result;
    }
    
    
        
 
        

                
}
