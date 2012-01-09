package util.collections.graph.defaultimpl;

import util.Utils;
import util.dbg.Logger;


/** 
 * An undirected edge, untyped. Parametrized by the key class and impl class 
 * of node. A node impl provides storing of neighbors and attributes, and is 
 * also itself be parametrized by the key class. 
 * 
 * Override this class to provide handling of edge attributes and type if needed.
 * 
 * Caution: Great care must be taken when implementing the setting of edge
 * attributes, as setting an attribute to an edge should (almost) always also 
 * set the corresponding attribute of the reverse edge, which always exists! 
 * 
 * As said, the edges are always undirected. However, an unfortunate fact is 
 * that we still always actually have to represent the edges in one or the other 
 * direction, a phenomenon we concisely call the "rep dir". We denote the 
 * (undirected) edge between nodes a and b by {a,b} or {b,a}, depending on the
 * rep dir. Note that we use the curly braces to indicate that the edges are 
 * undirected. 
 * 
 * TODO: We shall be going to move towards a brighter future by representing
 * each undirected edge in both directions. The "complement" edges are
 * going to refer to each other, and are going to share their attributes.
 * Let's NOT fix the representation direction; however, to later enable having
 * reps also in the other dir... If we are to have to "complement" reps,
 * they should at least share their attributes, and maybe refer to each
 * other. We might also represent the "non-canonical" version as a wrapper 
 * to the canonical version, which might be the most sensible case...
 * But then we would need a separate factory for those as well...

 * Note that it may not be possible to completely prevent creation of duplicate 
 * edges. However, they should only be created by
 * util.collections.graph.defaultimpl.Graph, and even then discarded immediately
 * as it is found out that the corresponding edge already exists.
 * 
 * Only util.collections.graph.defaultimpl.Graph is allowed to create Edges.
 *   
 * Parametrized by node key and node rep types.
 * 
 * The edge factory does not need to know how to create node instances.
 * However, it has to know what it the node impl class.
 *  
 */
public class DefaultEdge<T, N extends DefaultNode<T>> extends AbstractEdge<T,N>{
    
         
    private static final Factory FACTORY = new Factory();     
    
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
    protected void initAttributes() {
        // no attributes and thus no action
        if (getClass() != DefaultEdge.class) {
            Logger.uniqueWarning("DefaultEdge.InitAttributes() not overridden in class "+getClass());
        }
    }
    
    protected <E extends AbstractEdge<T,N>> void cloneNonTransientAttributes(E pDst) {
        // no attributes and thus no action
        if (getClass() != DefaultEdge.class) {
            Logger.uniqueWarning("DefaultEdge.cloneNonTransientAttributes() not overridden in class "+getClass());
        }
    }
            
    protected DefaultEdge(N pSrc, N pTgt) {        
        super(pSrc, pTgt);
    }
                   
    /** It is essential to override this in subclasses */
    public EdgeFactory getFactory() {
        if (getClass() != DefaultEdge.class) {
            Utils.die("DefaultEdge.getFactory() not overridden in class "+getClass());
            // Logger.uniqueWarning("DefaultEdge.getFactory() not overridden in class "+getClass());
        }
        return DefaultEdge.FACTORY;
    }       
    
    public static class Factory<T,N extends DefaultNode<T>> implements EdgeFactory<T, N, DefaultEdge<T,N>> {
        public DefaultEdge<T, N> makeEdge(N pSrc, N pTgt) {
            return new DefaultEdge(pSrc, pTgt);
        }
    }              
    
}
