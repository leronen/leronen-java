package util.collections.graph.defaultimpl;

import java.util.List;

import util.IOUtils;
import util.algorithm.ttnr.DefaultAttributedEdge;
import util.collections.SymmetricPair;


/** This is an minimal example on how to implement edges with custom attributes */ 
public class AttributedEdgeTest {
    
    private static final EdgeFactory NAMED_EDGE_FACTORY = new NamedEdgeFactory();
    
    public static void main(String[] args) throws Exception {
        // namedEdgeTest(args);
        defaultEdgeTest(args);
    }
            
    public static void namedEdgeTest(String[] args) throws Exception {
                      
        DefaultGraph<String, DefaultNode<String>, NamedEdge<String>> g = 
            new DefaultGraph(new DefaultNode.Factory(),
                             new NamedEdgeFactory());
                                                                 
        List<SymmetricPair<String>> nodePairs = IOUtils.readPairs(System.in);
        int nEdges = 0;
        
        for (SymmetricPair<String> pair: nodePairs) {
            NamedEdge<String> e = g.addOrGetEdge(pair, DefaultGraph.EdgeAddPolicy.WARN_IF_EXISTS);
            e.attributes.name = "edge"+(++nEdges);
        }
        
        System.out.println("Original graph:");
        System.out.println(g);

        DefaultGraph<String, DefaultNode<String>, NamedEdge<String>> clone = g.createClone();
        System.out.println("Clone:");
        System.out.println(clone);
        
        NamedEdge<String> firstEdge = clone.getEdges().iterator().next();
        firstEdge.attributes.name = "The first edge";
        System.out.println("Clone after renaming first edge:");
        System.out.println(clone);
        
        System.out.println("Original graph after renaming first edge: ");
        System.out.println(g);
                
        System.out.println("Edges of clone in reverse order:");
        StringBuffer sb = new StringBuffer();
        for (NamedEdge<String> e : clone.getEdges()) {                                       
            sb.append(e.getReverseEdge().toString());                                  
            sb.append("\n");
       }
       
       System.out.println(sb);
        
    }
        
    public static void defaultEdgeTest(String[] args) throws Exception {
                      
        DefaultGraph<String, DefaultNode<String>, DefaultAttributedEdge<String, DefaultNode<String>>> g = 
            new DefaultGraph(new DefaultNode.Factory(),
                             DefaultAttributedEdge.FACTORY);
                                                                 
        List<SymmetricPair<String>> nodePairs = IOUtils.readPairs(System.in);
        int nEdges = 0;
        
        for (SymmetricPair<String> pair: nodePairs) {
            DefaultAttributedEdge<String, DefaultNode<String>> e = g.addOrGetEdge(pair, DefaultGraph.EdgeAddPolicy.WARN_IF_EXISTS);
            e.attributes.put("name", "edge"+(++nEdges));
        }
        
        System.out.println("Original graph:");
        System.out.println(g);

        DefaultGraph<String, DefaultNode<String>, DefaultAttributedEdge<String, DefaultNode<String>>> clone = g.createClone();
        System.out.println("Clone:");
        System.out.println(clone);
        
        DefaultAttributedEdge<String, DefaultNode<String>> firstEdge = clone.getEdges().iterator().next();
        firstEdge.attributes.put("name", "The first edge");
        System.out.println("Clone after renaming first edge:");
        System.out.println(clone);
        
        System.out.println("Original graph after renaming first edge: ");
        System.out.println(g);
                
        System.out.println("Edges of clone in reverse order:");
        StringBuffer sb = new StringBuffer();
        for (DefaultAttributedEdge<String, DefaultNode<String>> e : clone.getEdges()) {                                       
            sb.append(e.getReverseEdge().toString());                                  
            sb.append("\n");
       }
       
       System.out.println(sb);
        
    }

    
    private static class NamedEdge<K> extends AttributedEdge<K, DefaultNode<K>, Attributes> {
        
        public NamedEdge(DefaultNode<K> pSrc,
                         DefaultNode<K> pDst) {
            super(pSrc, pDst);
        }
        
        protected Attributes makeAttributes() {
            return new Attributes();
        }
        
        protected <E extends AbstractEdge<K, DefaultNode<K>>> void cloneNonTransientAttributes(E pDst) {            
            NamedEdge<K> dst = (NamedEdge<K>)pDst;
            dst.attributes.name = attributes.name;
        }

        
        public EdgeFactory<K, DefaultNode<K>, NamedEdge<K>> getFactory() {            
            return NAMED_EDGE_FACTORY;
        }
        
    }

    private static class NamedEdgeFactory<K> implements EdgeFactory<K, DefaultNode<K>, NamedEdge<K>> { 
        public NamedEdge<K> makeEdge(DefaultNode<K> pSrc, DefaultNode<K> pDst) {
            return new NamedEdge(pSrc, pDst);
        }
    }
        
    
    public static class Attributes {
        String name;
        
        public String toString() {
            return "name="+name;
        }
    }
}
