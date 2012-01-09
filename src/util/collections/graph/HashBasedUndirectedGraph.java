package util.collections.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import util.StringUtils;
import util.collections.IPair;
import util.collections.MultiMap;
import util.collections.Pair;
import util.dbg.Logger;

/**
 * A straighforward use of a (hash-based) MultiMap to implement an undirected graph.
 * 
 */
public class HashBasedUndirectedGraph <T> implements IUndirectedGraph<T> {

    /** Core data */
    private MultiMap<T,T> mData;
    
    /**
     * As we actually use directed pairs to represent undirected pairs,
     * it must be remembered here, which way we are going to present them
     * in edges() results. Let's see how long it takes before we have to 
     * change this into a map. We cannot e.g. directly use this to test whether
     * a edge exist; we have to test both directions explicitly. 
     */
    private Set<IPair<T,T>> mEdgeSet;    
    
        
    public HashBasedUndirectedGraph() {
        mEdgeSet = new HashSet();
        mData = MultiMap.makeHashMapBasedMultiMap();        
    }
    
    public void addEdge(T p1, T p2) {        
        Pair<T,T> edge = new Pair(p1, p2);
        Pair<T,T> reverseEdge = new Pair(p2, p1);
        
        if (mEdgeSet.contains(edge) || mEdgeSet.contains(reverseEdge)) {
            Logger.warning("Discarding duplicate edge: "+edge);
        }
        else {
            mEdgeSet.add(new Pair(p1, p2));
            mData.put(p1, p2);
            mData.put(p2, p1);
        }
    }
       
    public Set<T> followers(T p) {
        return mData.get(p);
    }
        
    public Collection<T> nodes() {
        return mData.keySet();
    }
    
    /**
     * Expensive? Not in this case, it would seem. 
     * Represent edges in the same dir as they were inserted.
     * 
     * Recall that this set does not recognize the same edge represented in 
     * different dir as the same, so use with caution!
     */
    public Set<IPair<T,T>> edges() {        
        return mEdgeSet;
    }
    
    public int numEdges() {
        return mEdgeSet.size();
    }
    
    public String toString() {
        return StringUtils.collectionToString(mEdgeSet);
    }
    
}
