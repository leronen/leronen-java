package util.collections.graph.defaultimpl;

import java.util.ArrayList;

/** 
 * A node with an arbitrary key (of type T).
 * 
 * Let's STRICTLY enforce that there shall be at exactly one node instance per
 * key in each graph. Therefore, in the foreseeable future, we shall resort to 
 * object identity for equality and hashing.
 * 
 * But: can we enforce that also the key objects shall be unique?!?!? 
 * The answer is NO, as that would place too much responsibility on the caller.
 * 
 * For the same reason, we shall be able to use Node for storing attributes and
 * neighbors, etc...
 * 
 * Whenever a new instance of a Graph is created, we shall need to create 
 * new Node instances. Actually, Node:s should not be visible outside
 * the graph, and all access to nodes should occur through the graph interface.
 * 
 * A node thus belongs to EXACTLY ONE graph.
 * 
 * Note that edges are not publicly accessible from the node, but instead must
 * be queried through the graph.
 * 
 * Also note that DefaultNode objects cannot be shared across graphs; each node
 * should appear in exactly 1 graph; this is because the edge list is stored
 * directly into the node as a member variable. Actually, one should not
 * think of this class as the "node", but instead just a wrapper for the 
 * "real" node (mKey), holding the required additional data (edge list
 * and graph-derived attributes). 
 * 
 * 
 */
public class DefaultNode<T> {
    
    T mKey;    
    
    DefaultGraph mGraph;
                     
    
    protected ArrayList<AbstractEdge<T, DefaultNode<T>>> mEdges;    
    
    /** 
     * Create a node with a given key. Do not init mEdges yet; wait for some
     * clue on the node degree first. DefaultGraph is responsible for calling
     * initEdgeList() later.
     */
    protected DefaultNode(T pKey) {
        mKey = pKey;        
    }
    
    void initEdgeList(int pDegree) {
        mEdges = new ArrayList(pDegree);
    }
    
    /** Init edge list with no clue on the degree */
    void initEdgeList() {
        mEdges = new ArrayList(10);
    }
   
    public DefaultGraph getGraph() {
        return mGraph;
    }
    
    public String toString() {
        return mKey.toString();
    }
    
    public T getKey() {
        return mKey;
    }
    
    
    void addEdge(AbstractEdge<T, DefaultNode<T>> pE) {        
        mEdges.add(pE);
    }
    
    // Note no hashCode nor equals! This is because each node instance shall 
    // be unique and managed by the graph, using the key class T
//    public int hashCode() {
//        return mHashCode;
//    }
//    
//    public boolean equals(Object p) {
//        return mNode == 
//    }
    
    public static class Factory<T> implements NodeFactory<T,DefaultNode<T>> {
        public DefaultNode<T> makeNode(T p) {
            return new DefaultNode(p);
        }
    }
}
