package util.collections.graph;


import java.util.*;

import util.CollectionUtils;
import util.StringUtils;
import util.collections.MultiMap;
import util.collections.Pair;

/**
 * A bipartite graph unelegantly implemented with 2 multimaps. 
 * Not suitable for heavy algorithmic use or very large graphs, as the current impl
 * is quite wasteful.
 *   
 * TODO:
 *  - permit using this with different kind of Set and Map Implementations.
 *  - permit more efficient management of the edge and node sets.
 *    
 * Search terms: two-way map, bidirectional map, bidirectional map, bidir map,
 * reverse map, bipartite graph. 
 * 
 */
public class BipartiteGraph<T1,T2> implements Iterable<Pair<T1,T2>>{
    
    private MultiMap<T1,T2> forwardMap;
    private MultiMap<T2,T1> reverseMap;
        
    public BipartiteGraph() {
        forwardMap = new MultiMap<T1,T2>();
        reverseMap = new MultiMap<T2,T1>();
    }
    
    public BipartiteGraph(MultiMap<T1,T2> forwardMap) {
        this.forwardMap = forwardMap;
        this.reverseMap = new MultiMap<T2,T1>();
        for (T1 o1: forwardMap.keySet()) {
        	for (T2 o2: forwardMap.get(o1)) {        		           
        		this.reverseMap.put(o2, o1);
        	}        	
        }
    }
    
    /** Note that putting same mapping twice has no effect (no multiegdes) */
    public void put(T1 p1, T2 p2) {    	
    	forwardMap.put(p1, p2);
    	reverseMap.put(p2, p1);
    }    
               
    public void removeEdge(T1 o1, T2 o2) {
        forwardMap.remove(o1, o2);
        reverseMap.remove(o2, o1);
    }
            
    public Set<T2> followers(T1 p) {
        return forwardMap.get(p);
    }       
    
    public Set<T1> predecessors(T2 p) {
    	return reverseMap.get(p);
    }
    
    public Set<T1> getSrcNodes() {
    	return forwardMap.keySet();    	
    }
    
    public Set<T2> getTgtNodes() {
        return reverseMap.keySet();       
    }
     
    public boolean containsSrc(T1 o) {        
        return forwardMap.containsKey(o);
    }
    
    public boolean containsTgt(T2 o) {        
        return reverseMap.containsKey(o);
    }
    
    public boolean contains(Pair<T1,T2> mapping) {        
        return containsMapping(mapping.getObj1(), mapping.getObj2());
    }
    
    public boolean containsMapping(T1 p1, T2 p2) {
        if (forwardMap.containsKey(p1)) {
            Set<T2> values = forwardMap.get(p1);
            if (values.contains(p2)) {
                return true;                  
            }                       
        }
        
        return false;
    }    
        
    public static void main (String[] args) {
    	BipartiteGraph<Integer, String> g = new BipartiteGraph<Integer,String>();
        g.put(1, "A");
        g.put(1, "B");
        g.put(1, "C");
        g.put(2, "B");
        g.put(2, "C");
        g.put(2, "D");
        g.put(3, "E");
        g.put(4, "E");
        g.put(5, "E");
    	
        System.err.println("Created a test graph:\n"+g);
        System.err.println("source nodes:\n"+g.getSrcNodes());
        System.err.println("target nodes:\n"+g.getTgtNodes());
        System.err.println("Forward edges:\n"+g.forwardEdges());
        System.err.println("Reverse edges:\n"+g.reverseEdges());
        System.err.println("Forward multimap:\n"+g.forwardMap);        		
        System.err.println("Reverse multimap:\n"+g.reverseMap);
    	    	                  
    }
        
    public Iterator<Pair<T1,T2>> iterator() {
        return forwardMap.entryIterator();
    }
    
    public Iterator<Pair<T2,T1>> reverseIterator() {
        return reverseMap.entryIterator();
    }
    
    /** Inefficient, as edge set is not explicitly maintained */
    public Set<Pair<T1,T2>> forwardEdges() {
        return (Set<Pair<T1,T2>>)CollectionUtils.makeHashSet(iterator());
    }
    
    /** Inefficient, as edge set is not explicitly maintained */
    public Set<Pair<T2,T1>> reverseEdges() {
        return (Set<Pair<T2,T1>>)CollectionUtils.makeHashSet(reverseIterator());
    }

    public String dbgToString() {
    	Set<Pair<T1,T2>> edges = forwardEdges();               
        return StringUtils.collectionToString(edges);        
    }    
    
    public String toString() {        
        return StringUtils.multiMapToString(forwardMap, "=>", ",", "\n");        
    }
    
    
}

