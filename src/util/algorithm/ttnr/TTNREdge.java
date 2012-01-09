package util.algorithm.ttnr;

import util.collections.graph.defaultimpl.AbstractEdge;
import util.collections.graph.defaultimpl.AttributedEdge;
import util.collections.graph.defaultimpl.DefaultNode;
import util.collections.graph.defaultimpl.EdgeFactory;
import util.collections.graph.defaultimpl.IDijkstraEdge;

/**
 * An edge impl with attributes "prob", "iter" and "decided". Note that,
 * sadly enough, the variables are NOT named according to the honorable 
 * "m-prefix" tradition, like in "mThisIsAVariable".
 */  
public class TTNREdge<T> extends AttributedEdge<T,TTNRNode<T>, TTNREdgeAttributes> implements IDijkstraEdge {
    
    /**
     * The attributes are null by default; the caller (graph) is 
     * responsible for initializing the attributes after an edge has
     * been created.
     */
    
    static final Factory FACTORY = new Factory();
    
    public TTNREdge(TTNRNode<T> pSrc, TTNRNode<T> pTgt) {
        super(pSrc, pTgt);
    }
        
    protected TTNREdgeAttributes makeAttributes() {
        return new TTNREdgeAttributes();
    }
    
    public EdgeFactory getFactory() {
        return TTNREdge.FACTORY;
    }
          

    /**
     * Clone non-transient attributes from this edge to pDst. Used when
     * copying edges of one graph to another. It is assumed that attributes
     * have already been initialized (in initAttributes());
     */ 
    protected <E extends AbstractEdge<T,TTNRNode<T>>> void cloneNonTransientAttributes(E pDst) {
        ((TTNREdge<T>)pDst).attributes.prob = attributes.prob;
        ((TTNREdge<T>)pDst).attributes.len = attributes.len;
        ((TTNREdge<T>)pDst).attributes.prob_int = attributes.prob_int;               
    }

    
    public static class Factory<T,N extends DefaultNode<T>> implements EdgeFactory<T, TTNRNode<T>, TTNREdge<T>> {
        public TTNREdge<T> makeEdge(TTNRNode<T> pSrc, TTNRNode<T> pTgt) {
            return new TTNREdge(pSrc, pTgt);
        }
    }

    /** This shall be the length of the node (-log(prob)) */
    public double getLen() {        
        return attributes.len;
    }

    public void setLen(double pDist) {
        attributes.len = pDist;
        
    }

    
}

