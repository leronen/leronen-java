package util.collections.graph.defaultimpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


import util.IOUtils;
import util.collections.Triple;
import util.converter.StringToFloatConverter;

public class SimpleDijkstraGraph extends DefaultGraph<String, DijkstraNode<String>, WeightedEdge<String,DijkstraNode<String>>> {
    
    public SimpleDijkstraGraph() {
        super(new DijkstraNode.Factory(), new WeightedEdge.Factory());
    }
    
    /** Read from sgraph file transform goodness with -log into lenghts */
    public SimpleDijkstraGraph(String pFileName) throws IOException {
        
        this();
                        
        List<Triple<String,String, Float>> rows = 
            IOUtils.readTriples(new FileInputStream(pFileName),
                                null,
                                null,
                                new StringToFloatConverter());
                                                  
        for (Triple<String,String, Float> row: rows) {
            WeightedEdge<String, DijkstraNode<String>> edge = addOrGetEdge(row.getObj1(), row.getObj2(), DefaultGraph.EdgeAddPolicy.WARN_IF_EXISTS);
            double goodness = row.getObj3();
            edge.setLen(-Math.log(goodness));            
        }
        
        // do not forget this after read:
        compactEdges();
        
        for (DijkstraNode<String> n: getNodes()) {
            n.mDistance = Float.MAX_VALUE;
        }              
    }
    
    
    
    
}
