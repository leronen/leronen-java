package util.collections.graph.defaultimpl;

public interface IDijkstraNode {
    
    public double getDist();
    public void setDist(double pDist);
    /** Number of edges on the best path*/
    public void setNEdgesDist(int pDist);
    /** Number of edges on the best path */
    public int getNEdgesDist();
    
    /** invariant: pEdge.src == this */
    public AbstractEdge getParentLink();
    
    /** invariant: pEdge.src == this */ 
    public void setParentLink(AbstractEdge pEdge);
}
