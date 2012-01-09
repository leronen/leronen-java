package util.collections.graph;

/**
 * A edge of a undirected graph. Note that the nodes unfortunately
 * have to be represented in one direction or other.
 * 
 * Assume that sensible hashing and hashcode are implemented.
 * (u,v) should equal (v,u), and only one representation should actually
 * be used in any one graph. 
 */
public interface IEdge {
    public INode getNode1();
    public INode getNode2();
}
