package util.collections.graph.defaultimpl;

import java.util.Collection;

import util.factory.Factory;

/**
  * Again, care-free usage of the "pseudo-typedef" antipattern:
  * 
  * Notice, that a DefaultPath<T, DijkstraNode<T>, DijkstraEdge<T>> is not a DijkstraPath,
  * and can not be cast as such (of course, manual conversion is possible),
  * as would be the case with "proper" typedefs, if such an advanced
  * concept were to exist in java.
  *  
  */
public class DijkstraPath<T> extends DefaultPath<T, DijkstraNode<T>, WeightedEdge<T,DijkstraNode<T>>> {

    
    public Double mProb; 
            
    
    public void setProb(double pProb) {
        mProb = pProb;
    }
    
    public Double getProb() {
        return mProb;
    }
    
    public DijkstraPath() {
        super();
    }
    
    /**
     * Construct a path from an edge list. The rep dir of the links must 
     * be "from s to t".
     */  
    public DijkstraPath(Collection<? extends WeightedEdge<T,DijkstraNode<T>>> pEdges) {
        super(pEdges);
    }
    
    public static class DijkstraPathFactory<E> implements Factory<DijkstraPath<E>> {
        
        public DijkstraPath<E> makeObject() {
            return new DijkstraPath<E>();
        }
        
    }
    
   
}
 