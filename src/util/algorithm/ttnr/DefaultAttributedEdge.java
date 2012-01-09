package util.algorithm.ttnr;

import java.util.HashMap;

import util.collections.graph.defaultimpl.AbstractEdge;
import util.collections.graph.defaultimpl.AttributedEdge;
import util.collections.graph.defaultimpl.DefaultNode;
import util.collections.graph.defaultimpl.EdgeFactory;

/**
 * Edge with attributes implemented by a HashMap. 
 */  
public class DefaultAttributedEdge<T,N extends DefaultNode<T>> extends AttributedEdge<T,N,HashMap>  {
            
    public static final Factory FACTORY = new Factory();
    
    public DefaultAttributedEdge(N pSrc, N pTgt) {
        super(pSrc, pTgt);
    }
        
    protected HashMap makeAttributes() {
        return new HashMap();
    }
    
    public EdgeFactory getFactory() {
        return DefaultAttributedEdge.FACTORY;
    }
          

    /**
     * Clone non-transient attributes from this edge to pDst. Used when
     * copying edges of one graph to another. It is assumed that attributes
     * have already been initialized (in initAttributes());
     * 
     * Here, we assume that all attributes are non-transient, and shall be cloned
     * along with the edge when adding an edge to another graph...
     */ 
    protected <E extends AbstractEdge<T,N>> void cloneNonTransientAttributes(E pDst) {
        DefaultAttributedEdge<T,N> dst = (DefaultAttributedEdge<T,N>)pDst;
        for (Object key: attributes.keySet()) {
            Object val = attributes.get(key);
            dst.attributes.put(key, val);
        }
        
    }

    
    public static class Factory<T,N extends DefaultNode<T>> implements EdgeFactory<T, N, DefaultAttributedEdge<T,N>> {
        public DefaultAttributedEdge<T,N> makeEdge(N pSrc, N pTgt) {
            return new DefaultAttributedEdge(pSrc, pTgt);
        }
    }
    
    
}

