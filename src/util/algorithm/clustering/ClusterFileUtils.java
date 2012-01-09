package util.algorithm.clustering;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import util.IOUtils;
import util.IteratorIterable;
import util.StringUtils;
import util.collections.HashWeightedSet;
import util.collections.WeightedSet;

public class ClusterFileUtils {

//    public static <T> void writeClusters(Ma<Set<T>, Double> pSets) {
//        // todo
//        return null;
//    }
    
    
    /**
     * read clusters from file where col1 is the cluster "weight" and
     * rest of cols are elems of the cluster.
     * 
     */
    public static WeightedSet<Set<String>> readWeightedClusters(String pFile) throws IOException {
        
//        HashMap<Set<String>, Double> result = new HashMap();
        WeightedSet<Set<String>> result = new HashWeightedSet<Set<String>>();
        
        for (String line: new IteratorIterable<String>(IOUtils.lineIterator(pFile))) {
            String[] cols = StringUtils.fastSplit(line, ' ');
            Double w = new Double(cols[0]);
            Set<String> elems = new HashSet<String>(Arrays.asList(cols).subList(1, cols.length));
            result.add(elems, w);
        }
        
        return result;
    }
    
}
