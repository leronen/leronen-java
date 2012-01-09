package util.collections.graph;

import util.*;
import util.collections.graph.defaultimpl.DefaultGraph;
import util.collections.graph.DirectedGraph;
import util.collections.graph.IGraph;
import util.collections.IPair;
import util.collections.MultiMap;
import util.collections.OneToOneBidirectionalMap;
import util.collections.Pair;
import util.dbg.*;

import java.util.*;

/**
 * A graph unelegantly implemented with 2 multimaps (The reverse mappings are 
 * also stored to enable following links in the reverse direction). Not
 * suitable for heavy algorithmic use or large graphs, as the current impl
 * is quite wasteful.
 * 
 * Assumes objects in both "partitions" to be of the same class.
 *   
 * TODO:
 *  - permit using this with different kind of Set and Map Implementations.
 *  - permit more efficient management of the edge and node sets.
 *  - check the relation to class DefaultGraph (which is an undirected graph)
 *   with a way more efficient impl.
 *  
 * Search terms: two-way map, bidirectional map, bidirectional map, bidir map,
 * reverse map, bipartite graph, 
 * 
 * @see DirectedGraph
 * @see DefaultGraph
 * @see IGraph
 * @see MultiMap
 * @see OneToOneBidirectionalMap
 * @see NonInjectiveBidirectionalMap
 */
public class DirectedGraph<E> implements Iterable<Pair<E,E>>, IGraph<E> {
    
    private MultiMap<E,E> mMap;
    private MultiMap<E,E> mInverseMap;
        
    public DirectedGraph() {
        mMap = new MultiMap<E,E>();
        mInverseMap = new MultiMap<E,E>();
    }
    
    public DirectedGraph(MultiMap<E,E> pData) {
        mMap = pData;
        // desperately try to construct a MultiMap with sensible default capacities
        // in its data structures...
        int numValues = new HashSet(pData.values()).size();
        int invMMMapCapacity = numValues;
        int invMMSetCapacity = (int)(((double)(pData.keySet().size()))/ numValues)+1;
        mInverseMap = CollectionUtils.inverseMultiMap(mMap, new MultiMap(invMMMapCapacity, invMMSetCapacity));
    }
    
    public void put(E p1, E p2) {
    	if (mMap.containsKey(p1)) {
            Set<E> values = mMap.get(p1);
            if (values.contains(p2)) {
                Logger.warning("Re-putting mapping: "+p1+" => "+p2);                  
            }    		    		
    	}
    	mMap.put(p1, p2);
    	mInverseMap.put(p2, p1);
    }    
   
    public int outDegree(E p) {
        return followers(p).size();
    }
    
    
    
    public void removeEdge(E o1, E o2) {
        mMap.remove(o1, o2);
        mInverseMap.remove(o2, o1);
    }
    
    public int inDegree(E p) {
        return predecessors(p).size();
    }
    
    public Set<E> followers(E p) {
        return mMap.get(p);
    }       
    
    public Set<E> predecessors(E p) {
    	return mInverseMap.get(p);
    }
    
    public Set<E> getSrcNodes() {
    	return mMap.keySet();    	
    }
    
    public Set<E> getTgtNodes() {
        return mInverseMap.keySet();       
    }
    
    /** Note that the direction of the pair must be correct (from s to t) */
    public boolean isOneToOne(Pair<E,E> p) {
        if (!contains(p)) {
            throw new NoSuchElementException();
        }
        else {
            E s = p.getObj1();
            E t = p.getObj2();
            Set<E> tgtSet = mMap.get(s);
            Set<E> srcSet = mInverseMap.get(t);
            return tgtSet.size() == 1 && srcSet.size() == 1;
        }
    }
    
    /** 
     *  Return true when first element maps to more than one element (including 
     *  the second element given as parameter), and second 
     *  element does not map to other elements than the first element.
     *  Note that the direction of this must be correct (from set1 to set2) */
    public boolean isOneToMany(Pair<E,E> p) {
        if (!contains(p)) {
            throw new NoSuchElementException();
        }
        else {
            E s = p.getObj1();
            E t = p.getObj2();            
            Set<E> tgtSet = mMap.get(s);
            Set<E> srcSet = mInverseMap.get(t);
            return srcSet.size() == 1 && tgtSet.size() > 1;
        }
    }
    
    /** 
     *  Return true when second element maps to more than one element (including 
     *  the first element given as parameter), and first 
     *  element does not map to other elements than the second element.
     *  Note that the direction of this must be correct (from set1 to set2) */
    public boolean isManyToOne(Pair<E,E> p) {
        if (!contains(p)) {
            throw new NoSuchElementException();
        }
        else {
            E s = p.getObj1();
            E t = p.getObj2();
            Set<E> tgtSet = mMap.get(s);
            Set<E> srcSet = mInverseMap.get(t);
            return srcSet.size() > 1 && tgtSet.size() == 1;
        }
    }
    
    public boolean contains(Pair<E,E> p) {        
        return contains(p.getObj1(), p.getObj2());
    }
    
    public boolean contains(E p1, E p2) {
        if (mMap.containsKey(p1)) {
            Set<E> values = mMap.get(p1);
            if (values.contains(p2)) {
                return true;                  
            }                       
        }
        
        return false;
    }    
        
    public static void main (String[] args) {
    	DirectedGraph<Integer> g = new DirectedGraph();
        g.put(4, 1);
        g.put(3, 1);
        g.put(2, 1);
        g.put(5, 2);
        g.put(6, 2);
        g.put(7, 8);
    	
        System.err.println("Created a test graph:\n"+g);
        System.err.println("Nodes:\n"+g.nodes());
        System.err.println("Edges:\n"+g.edges());
    	    	                  
    }
        
    public Iterator<Pair<E,E>> iterator() {
        return mMap.entryIterator();
    }          
    
    /** Inefficient, as edge set is not explicitly maintained */
    public Set<IPair<E,E>> edges() {
        return (Set<IPair<E,E>>)(Object)CollectionUtils.makeHashSet(iterator());
    }

    public String dbgToString() {
        Iterator<Pair<E,E>> iter = mMap.entryIterator();
        List<Pair<E,E>> pairs = CollectionUtils.makeArrayList(iter);       
        
        List<String> reps = new ArrayList();
        for (Pair<E,E> pair: pairs) {
            String rep = ""+pair+" (isOneToOne:"+isOneToOne(pair)+")";
            reps.add(rep);
        }
        
        return StringUtils.listToString(reps);
        
    }
    

    
    /** Not very efficient, as node set is not explicitly maintained... */
    public Collection<E> nodes() {
        return CollectionUtils.union(getSrcNodes(), getTgtNodes());
    }
    
    public String toString() {        
        return StringUtils.multiMapToString(mMap, "=>", ",", "\n");        
    }
    
    
}
