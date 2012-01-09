package util.collections.graph.defaultimpl;

public class SimpleEdgeWeightedGraph extends DefaultGraph<String, DefaultNode<String>, WeightedEdge<String,DefaultNode<String>>> {
    
    public SimpleEdgeWeightedGraph() {
        super(new DefaultNode.Factory<String>(), new WeightedEdge.Factory<String,DefaultNode<String>>());
    }

    
    
}
