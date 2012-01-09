package util.collections.graph.defaultimpl;


import util.collections.graph.defaultimpl.AbstractEdge;
import util.collections.graph.defaultimpl.DefaultNode;

/**
 * An abstract impl for an edge with attributes stored as a instance of a separate
 * attributes class.  We sadly have to announce that we have here made the decision 
 * to keep this absrtact, which means that in addition to implementing the attribute class,
 * one needs also to implement the edge class by inheriting it from this class.
 * Main reason for this design is that somewhere, some details about the 
 * attributes implementation needs to be known; we have decided to place the
 * burden of that knowledge on the edge class. Let it be said, however, that we 
 * aim to keep the overhead of implementing these classes minimal.
 * 
 * So, to get a instantiable edge class with attributes, one needs to override this
 * class and implement two methods:
 *   - protected abstract A makeAttributes();
 *   - protected abstract void cloneNonTransientAttributes(AttributedEdge<K,N,A> pDst);
 *       * this is actually required already by AbstractEdge
 * 
 * Additionally, one needs to provide a factory for the edges (implement EdgeFactory),
 * and of course an implementation class for storing the actual attributes. 
 * 
 * @see DefaultAttributedEdge 
 * @see AttributedEdgeTest 
 * 
 * Type parameters:
 *   K = node key class
 *   N = node impl class
 *   A = attribute impl class
 *   
 */  
public abstract class AttributedEdge<K,N extends DefaultNode<K>,A> extends AbstractEdge<K,N> {        
            
    /**
     * The attributes are null by default; the caller (graph) is 
     * responsible for initializing the attributes after an edge has
     * been created.
     */
    public A attributes;
            
    protected AttributedEdge(N pSrc, N pTgt) {
        super(pSrc, pTgt);
    }
    
    /** 
     * Also inits attributes for the reverse, as required by some presumably
     * well-defined contract. 
     */
    protected void initAttributes() {
        attributes = makeAttributes();
        ((AttributedEdge)reverse).attributes = attributes;
    }
    
    public String toString() {
        if (attributes != null) {
            return super.toString()+" ("+attributes.toString()+")";
        }
        else {
            return super.toString();
        }
    }       

    /** Require this from subclasses */
    protected abstract A makeAttributes();
    
        
}


