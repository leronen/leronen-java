package util.collections.graph.defaultimpl;

/**
 * An EdgeFactory has the power to fabricate Edges. Note that this only 
 * provides the edges with the very basic data: the nodes. Reverse reps are 
 * created by the graph, and the graph also links the complementary reps together.
 * Edge is responsible for creating it's own (potential) attributes in method
 * initAttributes, which is called by the graph at an appropriate stage.
 * Graph also sets the graph link of an edge...
 *     
 * 
 */
public interface EdgeFactory<T, N extends DefaultNode<T>, E extends AbstractEdge<T,N>> {
    public E makeEdge(N pSrc, N pTgt);
}
