package util.collections.graph.defaultimpl;

import java.util.List;
import java.util.Set;

import util.IOUtils;
import util.collections.SymmetricPair;

public class Tester {
    
    public static void main(String[] args) throws Exception {
        // basicTest();
        // weightedTest();
        // new Tester().dfsTest(args);
        new Tester().connCompTest(args);
    }
        
    private void connCompTest(String[] args) throws Exception {
        SimpleGraph g = new SimpleGraph();
                       
        List<SymmetricPair<String>> nodePairs = IOUtils.readPairs(System.in);
        for (SymmetricPair<String> pair: nodePairs) {
            g.addOrGetEdge(pair, DefaultGraph.EdgeAddPolicy.WARN_IF_EXISTS);           
        }
        g.compactEdges();
                               
        GraphAlgorithms_SimpleGraph ga = new GraphAlgorithms_SimpleGraph();
        List<Set<DefaultNode<String>>> connectedComponents = 
            ga.connectedComponents(g);
        System.out.println("There are "+connectedComponents.size()+" connected components");    
         
        // System.out.println(g);
    }
    
    
}