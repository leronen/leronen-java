package util.algorithm.ttnr;

import java.util.Collection;

import util.collections.graph.defaultimpl.DefaultPath;
import util.factory.Factory;

/**
  * Again, care-free usage of the "pseudo-typedef" antipattern.
  * 
  * Notice, that a DefaultPath<T, TTNRNode<T>, TTNREdge<T>> is not a TTNRPath,
  * and can not be cast as such (of course, manual conversion is possible),
  * as would be the case with "proper" typedefs, if such an advanced
  * concept were to exist in java.
  *  
  */
public class TTNRPath<T> extends DefaultPath<T, TTNRNode<T>, TTNREdge<T>> {

    public TTNRPath() {
        super();
    }
    
    /**
     * Construct a path from an edge list. The rep dir of the links must 
     * be "from s to t".
     */  
    public TTNRPath(Collection<? extends TTNREdge<T>> pEdges) {
        super(pEdges);
    }
    
    public static class TTNRPathFactory<E> implements Factory<TTNRPath<E>> {
        
        public TTNRPath<E> makeObject() {
            return new TTNRPath<E>();
        }
        
    }
    
   
}
 