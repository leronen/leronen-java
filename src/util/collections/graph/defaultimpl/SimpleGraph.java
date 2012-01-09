package util.collections.graph.defaultimpl;

public class SimpleGraph extends DefaultGraph<String, DefaultNode<String>, DefaultEdge<String,DefaultNode<String>>> {
    
    public SimpleGraph() {
        super(new DefaultNode.Factory<String>(), new DefaultEdge.Factory<String,DefaultNode<String>>());
    }

    
    
}
