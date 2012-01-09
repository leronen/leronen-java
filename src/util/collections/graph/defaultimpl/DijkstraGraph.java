package util.collections.graph.defaultimpl;


public class DijkstraGraph<T> extends DefaultGraph<T, DijkstraNode<T>, WeightedEdge<T,DijkstraNode<T>>> {
    
    public DijkstraGraph() {
        super(new DijkstraNode.Factory(), new WeightedEdge.Factory());
    }
    
//    /** Read from sgraph file */
//    public SimpleDijkstraGraph(T pFileName) throws IOException {
//        
//        this();
//                        
//        List<Triple<String,String, Float>> rows = 
//            IOUtils.readTriples(new FileInputStream(pFileName),
//                                null,
//                                null,
//                                new StringToFloatConverter());
//                                                  
//        for (Triple<String,String, Float> row: rows) {
//            WeightedEdge<String, DijkstraNode<String>> edge = addEdge(row.getObj1(), row.getObj2(), DefaultGraph.EdgeAddPolicy.WARN_IF_EXISTS);
//            edge.mWeight = row.getObj3();            
//        }
//        
//        // do not forget this after read:
//        compactEdges();
//        
//        for (DijkstraNode<String> n: getNodes()) {
//            n.mDistance = Float.MAX_VALUE;
//        }              
//    }
    
//    public void convertWeights(Converter<Double,Double> pConverter) {
//        for (WeightedEdge e: getEdges()) {
//            e.setWeight(pConverter.convert(e.getWeight());
//        }
//    }

    
    
}
