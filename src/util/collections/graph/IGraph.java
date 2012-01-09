package util.collections.graph;

import java.util.Collection;
import java.util.Set;

import util.collections.IPair;


/**
 * The basic graph interface for common graph utilities. Encompasses both
 * directed and undirected graphs.
 *  
 * Implementations need not always implement everything, just the ones that shall 
 * actually be needed.
 * 
 * Actually, we gravely need to have explicit objects for edges also,
 * if we are to have any kind of edge attributes...
 * 
 * One simple way to implement this would be to have some interface IEdge<T>,
 * where T is the node type. Note that we would insist on the model that
 * for undirected edge types, (n1,n2,t) == (n2,n1,t), and for directed
 * edge types, (n1,n2,t) == (n2,n1,-t). This way, edges only need to 
 * be stored in one direction.
 * 
 * And what about multigraphs?
 * 
 * Contents of IEdge, then?
 *   * getNode1()
 *   * getNode2()
 *   * getOtherNode(T)
 *   
 */
public interface IGraph<T> {
    
    /** Should actually represent a Set, but is a Collection for some historical reasons */
    public Collection<T> nodes();
    
    /** 
     * In an undirected graph, it always holds that
     * "A in followers(B) <=> B in followers(A)" 
     */
    public Iterable<T> followers(T p);
    
    /** 
     * May be expensive or even Unsupported, as the set of edges may not be 
     * maintained explicitly in all implementations, or even be represented 
     * with anything even remotely resembling IPairs.
     * 
     * For undirected graphs, should never return the same edge in both directions
     * (we actually should insist that in the IPair implementation, the inverse 
     * edges are equal, as defined by both equals and hashcode). This may 
     * be hard to meet in practice, though...
     * Actually, we should not use IPair to represent undirected entities!
     * It is a pity that there is no good interface for undirected pairs;
     * set wont do, neither does IPair (or List).
     *  
     * Let's try to keep the use for this to an absolute minimum, while carefully
     * avoiding any grave consequences (this is bound to crash sometime in the future...)
     * e.g. do not use this to test whether an edge exists in the graph...
     */
    public Set<IPair<T,T>> edges();
}
