package util.algorithm.classification;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.CollectionUtils;
import util.IOUtils;
import util.SU;
import util.Utils;
import util.collections.MultiMap;
import util.collections.UnorderedPair;
import util.dbg.Logger;

public class KNNUtils {        
    
    /** Pairs within anchor are "pos", anchor-other-pairs are "neg" */
    public static <T> Map<UnorderedPair<T>, BinaryClassification> formPairClassifications(Set<T> pAnchor, Set<T> pNullNodes) {
        
        if (CollectionUtils.intersects(pAnchor, pNullNodes)) {
            throw new IllegalArgumentException(
                    "anchor and null sets intersect! (intersection: "+
                    SU.toString(CollectionUtils.intersection(pAnchor, pNullNodes))+")");            
        }
        
        LinkedHashMap result = new LinkedHashMap();
        
        for (T p: pAnchor) {
            
            // form "positive" pairs from within-anchor pairs
            for (T n: pAnchor) {
                if (!(p.equals(n))) {
                    UnorderedPair<T> pair = new UnorderedPair<T>(p,n);
                    if (!result.containsKey(pair)) {
                        result.put(pair, BinaryClassification.POS);
                    }                   
                }
            }            
            
            
         // form "negative" pairs from anchor-null pairs
            for (T n: pNullNodes) {                
                UnorderedPair<T> pair = new UnorderedPair<T>(p,n);
                result.put(pair, BinaryClassification.NEG);                
            }
        }
        
        return result;
    }
     
    
    /** Paths are crudely represented as strings like "s=node1 t=node2..." */ 
    public static MultiMap<BinaryClassification, String> 
        formPathClassifications(Set<String> pAnchor, 
                                Set<String> pNullNodes,
                                List<String> pPaths) {
        
        Map<UnorderedPair<String>, BinaryClassification> pairClassifications = 
            formPairClassifications(pAnchor, pNullNodes);
        
        MultiMap<BinaryClassification, String> result = new MultiMap();
        
        for (String pathRep: pPaths) {
            // Logger.info("PathRep: "+pathRep);
            String[] tok = pathRep.split("\\s+");
            String s = tok[0].substring(2);
            String t = tok[1].substring(2);
            UnorderedPair<String> pair = new UnorderedPair<String>(s,t);
            BinaryClassification c = pairClassifications.get(pair);
            result.put(c, pathRep);
        }
        
        return result;
        
    }
    
    /**
     * 
     * if args[0] == "classify_pairs" 
     *   args[1]: anchor node file
     *   args[2]: null node file
     * 
     * if args[0] == "classify_paths"  
     *   args[1]: anchor node file (in)
     *   args[2]: null node file (in)
     *   args[3]: pathfile (in)
     *   args[4]: pospathfile (out)
     *   args[5]: negpathfile (out)
     * null node file may also include anchor nodes, which are however not
     * interpreted as null nodes, but are discarded instead.
     */
    public static void main(String[] args) throws Exception {
        
        String cmd = args[0];
        
        if (cmd.equals("classify_pairs")) {
            String anchorNodeFile = args[1];
            String nullNodeFile = args[2];
            Set<String> posNodes = new LinkedHashSet(IOUtils.readLines(anchorNodeFile));
            Set<String> nullNodes = new LinkedHashSet(IOUtils.readLines(nullNodeFile));
            nullNodes.removeAll(posNodes);
            
            Map<UnorderedPair<String>, BinaryClassification> classifications = 
                formPairClassifications(posNodes, nullNodes);
            
            for (UnorderedPair<String> pair: classifications.keySet()) { 
                System.out.println(pair.getObj1()+" "+pair.getObj2()+" "+classifications.get(pair));
            }
        }
        else if (cmd.equals("classify_paths")) {
            Logger.loudInfo("Classifying paths!");
            String anchorNodeFile = args[1];
            String nullNodeFile = args[2];
            String pathFile = args[3];
            String posPathFile = args[4];
            String negPathFile = args[5];
            Logger.info("anchorNodefile: "+anchorNodeFile);
            Logger.info("nullNodefile: "+nullNodeFile);
            Logger.info("pathFile: "+pathFile);
            Logger.info("pospathFile: "+posPathFile);
            Logger.info("negpathFile: "+negPathFile);
            
            Set<String> posNodes = new LinkedHashSet(IOUtils.readLines(anchorNodeFile));
            Set<String> nullNodes = new LinkedHashSet(IOUtils.readLines(nullNodeFile));
            nullNodes.removeAll(posNodes);
            
            List<String> paths = IOUtils.readLines(pathFile);
                        
            MultiMap<BinaryClassification, String> pathClassifications = 
                formPathClassifications(posNodes, nullNodes, paths);
            
            
            Logger.loudInfo("Got path classifications: "+pathClassifications);
            
            IOUtils.writeCollection(posPathFile, pathClassifications.get(BinaryClassification.POS));
            IOUtils.writeCollection(negPathFile, pathClassifications.get(BinaryClassification.NEG));                           
        }
        else {
            Utils.die("Unknown command: "+cmd);
        }
    }
    
    
}
