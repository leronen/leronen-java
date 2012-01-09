package util.collections.graph.defaultimpl;

public class DijkstraNode<T> extends DefaultNode<T> implements IDijkstraNode {
    
    DijkstraNode(T pKey) {
        super(pKey);
    }
    
    public double mDistance = Double.NaN;

    /** Number of edges on the best path */
    public int nEdgesDist;
    
    /** 
     * A parent link used during exection of the Dijkstra algorithm
     * Note that for efficiency, the rep dir of this is arbitrary;
     * use #Edge.startingFrom(Node) to get a "directed" rep when
     * needed.
     */
    public WeightedEdge<T, DijkstraNode<T>> mParentLink = null;
  
    public double getDist() {
        return mDistance;
    }
    
    public void setDist(double pDist) {
        mDistance = pDist;
    }
    
    /** invariant: pEdge.src == this */
    public DefaultEdge getParentLink() {
        return mParentLink;                                        
    }
    
    /** invariant: pEdge.src == this */ 
    public void setParentLink(AbstractEdge pEdge) {
        mParentLink = (WeightedEdge<T, DijkstraNode<T>>)pEdge;
    }
    

    @Override
    public int getNEdgesDist() {
        return nEdgesDist;
        
    }

    @Override
    public void setNEdgesDist(int dist) {
        nEdgesDist = dist;        
    }
    
    public static class Factory<T> implements NodeFactory<T, DijkstraNode<T>> {
        public DijkstraNode<T> makeNode(T pKey) {
            return new DijkstraNode(pKey);
        }
    }
}
