package util.collections.graph.defaultimpl;

   

/**
 * Used to create (initially) empty graphs. Each graph should be able to provide 
 * a factory to constructs instances of the same graph class.   
 *
 */
public interface GraphFactory<T, N extends DefaultNode<T>, E extends AbstractEdge<T,N>, G extends DefaultGraph<T,N,E>> {
    public G makeGraph();
}


