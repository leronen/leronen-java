package util.collections.graph.defaultimpl;

public interface NodeFactory<T, N extends DefaultNode<T>> {
    public N makeNode(T pKey);
}
