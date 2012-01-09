package util.collections.graph.defaultimpl;



/** 
 * An undirected, untyped edge. Parametrized by the key class and impl class 
 * of node. A node impl provides storing of neighbors and attributes, and is 
 * also itself be parametrized by the key class.
 * 
 * Note the ABSOLUTE restriction that an edge always belongs to EXACTLY ONE
 * GRAPH; the graph is responsible for setting the reference to the graph
 * (it is not the responsibility of the edge factory!) 
 * 
 * 
 * Override this class to provide handling of edge attributes and type if needed.
 * 
 * Caution: Great care must be taken when implementing the setting of edge
 * attributes, as setting an attribute to an edge should (almost) always also 
 * set the corresponding attribute of the reverse edge, which always exists!
 * If the attributes are handled by a dedicated attributes object, this will
 * likely be shared between the complementary instances, and shall not pose a problem. 
 * 
 * As said, the edges are always undirected. However, an unfortunate fact is 
 * that we still always actually have to represent the edges in one or the other 
 * direction, a phenomenon we concisely call the "rep dir". We denote the 
 * (undirected) edge between nodes a and b by {a,b} or {b,a}, depending on the
 * rep dir. Note that we use the curly braces to indicate that the edges are 
 * undirected. 
 *  
 * Note that it may not be possible to completely prevent creation of duplicate 
 * edges. However, they should only be created by
 * util.collections.graph.defaultimpl.Graph, and even then discarded immediately
 * as it is found out that the corresponding edge already exists.
 * 
 * Only util.collections.graph.defaultimpl.Graph is allowed to create Edges.
 * Oh, but this seems to be a grave restriction, given that one may want to 
 * handle only paths instead of a complete graph!
 *   
 * Parametrized by node key and node rep types.
 * 
 * The edge factory does not need to know how to create node instances.
 * However, it has to know what is the node impl class.
 * 
 * Note that edges from two different graphs SHOULD NOT be compared
 * for equality, as they will definitely be considered different.
 *  
 */
public abstract class AbstractEdge<T, N extends DefaultNode<T>> {
    
    /**
     * A horrible kludge, yes... needed, as sometimes
     * it would indeed (despite the loss in efficiency) be nice
     * not to require that the nodes are the very same objects in order
     * to consider the edges equal
     */
//    public static sRequireNodeReferenceEqualityInEquals = false;
    
    // let's start relaxing the hungarian naming conventions for public instance 
    // variables: 
    public N src;
    public N tgt;
    
    public DefaultGraph mGraph;
    
    // for private vars, the old convention is still optimal:
    private int mHashCode = -1;
    
    /**
     * The reverse rep for the same logical (undirected) edge. Should always be
     * set by the graph right after creating the reverse edge.
     * Recall also that an edge and the reverse edge should always be
     * created in tandem.
     * 
     * Let's not use the loathed "m" naming convention, as this shall be
     * exposed to clients extending AbstractEdge. 
     */  
    protected AbstractEdge<T,N> reverse;
           
    
    /**
     * DefaultGraph calls this after creating an edge. By default, 
     * an edge has no attributes. Subclasses should override this to provide
     * initialization for their attributes.
     * 
     * Note that attributes for the reverse MUST also be initialized here!
     * Invariant: it is mandatory that reverse has been set before calling this! 
     * 
     * 
     * Note that the attributes should in practice always(?) be implemented with
     * a separate "Attributes" class, which enables setting attributes for
     * both "reps" of the edge by using either of the reps.
     * 
     * Alternatively, but not recommendedly, the reps could synchronize their
     * attributes by some other means (it might (?) even be that some
     * attributes could be stored for the different reps separately...)
     */
    protected abstract void initAttributes();
    
    /** Called EXCLUSIVELY by the graph after creating edge */     
    protected void setGraph(DefaultGraph pGraph) {
        mGraph = pGraph;
    }
    
    public DefaultGraph getGraph() {
        return mGraph;
    }
     
    protected abstract <E extends AbstractEdge<T,N>> void cloneNonTransientAttributes(E pDst);   
            
    protected AbstractEdge(N pSrc, N pTgt) {        
        src = pSrc;
        tgt = pTgt;
        mHashCode = src.hashCode() + tgt.hashCode();
    }
    
    /**
     * Compute hashcode already at initialization time  
     * Note that {a,b} and {b,a} are considered equal.
     */
    public final int hashCode() {
        return mHashCode;        
    }
    
    /**
     * Note that {a,b} and {b,a} are considered equal.
     * Also note that this relies on the uniqueness of node instances (two different
     * node instances with the same key shall be considered different!)
     * 
     * Note that for typed edges (at least), this should be overridden.
     */
    public boolean equals(AbstractEdge<T,N> pEdge) {
        return (src == pEdge.src && tgt == pEdge.tgt)
            || (src == pEdge.tgt && tgt == pEdge.src);
    }
    
    /**
     * Note that {a,b} and {b,a} are considered equal.
     * Also note that this relies on the uniqueness of node instances (two different
     * node instances with the same key shall be considered different!)
     * 
     * Furthermore note that subclasses cannot change the way that equality is
     * defined.
     */
    public final boolean equals(Object p) {
       if (p instanceof AbstractEdge) {
           return equals((AbstractEdge)p);
       }
       else {
           return false;
       }
    }
        
    /**
     *  It is an error to call this with a node that is not and endpoint of this
     *  edge! 
     *  
     *  Note that again, this relies on the node objects being unique within each graph! 
     *   
     */
    public N getOtherNode(N pNode) {
        if (src == pNode) {
            return tgt;
        }
        else if (tgt == pNode ) {      
            return src;
        }
        else {
            throw new RuntimeException("No such node ["+pNode+"] in edge ["+this+"]");
        }
    }
        
    public <E extends AbstractEdge<T,N>> E getReverseEdge() {
        return (E)reverse;
    }
    
    public <E extends AbstractEdge<T,N>> E startingFrom(N pNode) {        
        if (src == pNode) {
            return (E)this;            
        }
        else if (tgt == pNode) {
            return (E)reverse;
        }
        else {
            throw new RuntimeException("Node ["+pNode+"] is not an endpoint of this edge ["+this +"] ");
        }       
    }
    
    public String toString() {
        return "{"+src+","+tgt+"}";
    }
        
    public abstract EdgeFactory getFactory();
        
                     
    
}
