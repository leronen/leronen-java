package util.collections.graph.defaultimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import util.StringUtils;

/**
  * An acyclic path where the (actually undirected) edges are represented in  
  * a specific direction, namely "from s to t", e.g. a s-t-path could consist
  * of following edges: {s,u} {u,v} {v,t}
  * 
  * Note that while in a Graph the edges are represented in arbitrary direction,
  * here they are always in "sensible" rep dir (complementary edges are created
  * on demand, which might amount to serious overhead).
  * 
  * Should path probability be included?
  * 
  * TODO: a more generic implementation, to make DefaultGraph aware of Paths? 
  */
public class DefaultPath<T,N extends DefaultNode<T>, E extends AbstractEdge<T, N>> extends ArrayList<E> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5840170577524410147L;

	public DefaultPath() {
        super();
    }
    
    /**
     * Enforce the invariant that src of pEdge must be the tgt of the last 
     * edge on this path.
     */
    public void append(E pEdge) {
        E last = get(size()-1);
        if (!(last.tgt == pEdge.src)) {
            throw new RuntimeException("Cannot add edge: "+pEdge+": edge src is not the last node on this path!");
        }
        
    }
    
    /**
     * Construct a path from an edge list. The rep dir of the links must 
     * be "from s to t".
     */  
    public DefaultPath(Collection<? extends E> pEdges) {
        super(pEdges);
    }
    
    /** Compute on demand */
    public List<N> nodes() {
        ArrayList result = new ArrayList(size()+1);
        result.add(get(0).src);
        for (E e: this) {
            result.add(e.tgt);
        }
        return result;
    }
    
    /**
     * Reverse the path in place. Might be costly until some refactorings 
     * Is this what is desired, or should the reverse be a new instance 
     * altogether?
     */
    public void reverse() {
        // reverse order of links
        Collections.reverse(this);
        
        // reverse the links themselves
        for (int i=0; i<size(); i++) {
            E straight = get(i);
            E reversed = (E)straight.getReverseEdge(); 
            set(i, reversed);
        }        
    }
    
    public int length() {
        return size();
    }
    
    public String toString() {
       return StringUtils.listToString(this, ", "); 
    }
    
    public N getSrc() {
        return get(0).src;
    }
    
    public N getTgt() {
        return get(size()-1).tgt;
    }
    

}
 