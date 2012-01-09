package util.algorithm.ttnr;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import util.collections.graph.defaultimpl.AbstractEdge;
import util.collections.graph.defaultimpl.DefaultNode;
import util.collections.graph.defaultimpl.IDijkstraNode;
import util.collections.graph.defaultimpl.NodeFactory;


/** 
 * A node which stores attributes related to reliability computations. 
 */
public class TTNRNode<T> extends DefaultNode<T> implements IDijkstraNode {

    public static final Factory FACTORY = new Factory();         
    
    public boolean reached;
    public int reachedCount;    
    public boolean isQueryNode;
    public double dist;
    public double reliability;
    /** Number of edges on the best path */
    public int nEdgesDist;
        
    public TTNRNode(T pKey) {
        super(pKey);        
        clearAttributes();
    }    
    
    public void clearAttributes() {
        reached = false;
        reachedCount = 0;
        isQueryNode = false; 
        dist = Double.NaN;
        reliability = Double.NaN;
        nEdgesDist = -1;
    }
    
    /**
     * To enable using query nodes for indexing tables (or even bitsets) 
     */
    public int ind;
    
    /** 
     * A parent link used during execution of the Dijkstra algorithm.
     * The rep direction of the parent link should always be (this,other).
     * Make this private in order to enforce that parentlink.src == this.
     */
    private TTNREdge<T> parentlink = null;
    
   /**
    * Set the parent link used during execution of Dijkstra's algorithm.
    * The rep direction of the parent link should always be (this,other).
    * A case where this is not the case is considered an error!
    */
    public void setParentLink(AbstractEdge pEdge) { 
        if (pEdge.src != this) {
            throw new RuntimeException("Invalid parent link for this node: src is not correct: "+pEdge);
        }
        parentlink = (TTNREdge<T>)pEdge;
    }
    
    public void sortEdgesAccordingToLen(boolean pShortestFirst) {
        List<TTNREdge<T>> edges = (List<TTNREdge<T>>)(List)mEdges;
        final int factor = pShortestFirst ? 1 : -1;
        Collections.sort(edges, new Comparator<TTNREdge<T>>() {
            public int compare(TTNREdge<T> p1, TTNREdge<T> p2) {
                double len1 = p1.attributes.len;
                double len2 = p2.attributes.len;
                double cmp = len1-len2;
                if (cmp < 0) {
                    return -factor;
                }
                else if (cmp > 0) {
                    return factor;                    
                }
                else {
                    return 0;
                }
            }
        });
        
//        Logger.info("");
//        Logger.info("Sorted edges of "+this+":\n");
//        for (DefaultEdge e: mEdges) {
//            Logger.info(""+((TTNREdge)e).attributes.len);
//        }
    }
    
    /** 
     * Sort edges according to the dist attribute of the target node. 
     * Note that with a priority queue, we want smallest to come first;
     * with a stack, we want the largest to come first, as naive stack-based
     * dfs causes the iteration order of children to be reversed!
     */
    public void sortEdgesAccordingToTargetDistance(boolean pSmallestFirst) {
        List<TTNREdge> edges = (List<TTNREdge>)(List)mEdges;
        final int factor = pSmallestFirst ? 1 : -1;
        Collections.sort(edges, new Comparator<TTNREdge>() {
            public int compare(TTNREdge p1, TTNREdge p2) {
//                double d1 = ((TTNRNode)(p1.tgt)).dist + p1.attributes.len;
//                double d2 = ((TTNRNode)(p2.tgt)).dist + p2.attributes.len;
                // note that performance is a bit better when the reliability
                // of edge is not taken into account (as would be done in
                // the commented code above)
                double d1 = ((TTNRNode)(p1.tgt)).dist;
                double d2 = ((TTNRNode)(p2.tgt)).dist;
                double cmp = d1-d2;
                if (cmp < 0) {
                    return -factor;
                }
                else if (cmp > 0) {
                    return factor;                    
                }
                else {
                    return 0;
                }
            }
        });
//        Logger.info("");
//        Logger.info("Sorted edges of "+this+":\n");
//        for (DefaultEdge e: mEdges) {
//            Logger.info(""+e.tgt);
//        }
    }
    
    public void sortEdgesAccordingToTargetTTNR(boolean pSmallestFirst) {
        List<TTNREdge> edges = (List<TTNREdge>)(List)mEdges;
        final int factor = pSmallestFirst ? 1 : -1;
        Collections.sort(edges, new Comparator<TTNREdge>() {
            public int compare(TTNREdge p1, TTNREdge p2) {
//                double d1 = ((TTNRNode)(p1.tgt)).dist + p1.attributes.len;
//                double d2 = ((TTNRNode)(p2.tgt)).dist + p2.attributes.len;
                // note that performance is a bit better when the reliability
                // of edge is not taken into account (as would be done in
                // the commented code above)
                double d1 = ((TTNRNode)(p1.tgt)).reliability;
                double d2 = ((TTNRNode)(p2.tgt)).reliability;
                double cmp = d1-d2;
                if (cmp < 0) {
                    return -factor;
                }
                else if (cmp > 0) {
                    return factor;                    
                }
                else {
                    return 0;
                }
            }
        });
//        Logger.info("");
//        Logger.info("Sorted edges of "+this+":\n");
//        for (DefaultEdge e: mEdges) {
//            Logger.info(""+((TTNRNode)e.tgt).ttnr);
//        }
    }
    
    
    
    /** The rep direction of the parent link should always be (this,other). */
    public TTNREdge<T> getParentLink() {
        return parentlink;
    }
    
    public TTNRNode<T> getParentNode() {
        return parentlink.tgt;
    }
  
    
    public double getDist() {
        return dist;
    }
    
    public void setDist(double pDist) {
        dist = pDist;
    }
    
    public String toString() {
//        return super.toString()+" [dist="+dist+"]";
        return super.toString();
    }
    
    @Override
    public int getNEdgesDist() {
        return nEdgesDist;
        
    }

    @Override
    public void setNEdgesDist(int dist) {
        nEdgesDist = dist;        
    }
    
    public static class Factory<T> implements NodeFactory<T, TTNRNode<T>> {
        public TTNRNode<T> makeNode(T pKey) {
            return new TTNRNode(pKey);
        }
    }
    
    
}
