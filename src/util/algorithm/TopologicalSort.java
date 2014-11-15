package util.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.CollectionUtils;
import util.IOUtils;
import util.Utils;
import util.collections.ArrayStack;
import util.collections.MultiMap;
import util.collections.SymmetricPair;
import util.collections.graph.DirectedGraph;
import util.dbg.Logger;

/**
 * Implement simple topological sorting along the following outlines 
 * found from the always-reliable www:
 *      1. Compute and store the in degree d[j] for each j (this is the number of i's for which i -> j).
 *      2. Initialise a collection (typically a queue or a stack, doesn't matter which) C to {j : d[j] == 0}.
 *      3. While C is not empty:
 *             * Remove some element i from C.
 *             * Output i.
 *             * For each j such that i -> j, decrement d[d]; if this zeros it, add j to C. 
 *      4. If we output n values in the previous step, the sort succeeded; otherwise it failed, and a loop exists in the input.
 * 
 * The actual implementation may not be the most efficient possible, so 
 * heavy-duty use of this is discouraged.
 * 
 * @author leronen
 *
 */
public class TopologicalSort {

    /**
     * Make a total ordering out of a partial ordering.
     * @param pPairs a list of pairs, where obj1 should precede obj2 in the
     * result ("o1 < o2").
     */
    public static <T> List<T> sort(List<SymmetricPair<T>> pPairs) throws CycleExistsException {
        MultiMap<T,T> mm = CollectionUtils.makeMultiMapFromSymmetricPairs(pPairs);
//        Logger.info("Made multi-map:\n"+StringUtils.multiMapToString(mm));        
        return sort(mm);
 
    }

    /**
     * Make a total ordering out of a partial ordering.
     * @param pPairs a list of pairs, where obj1 should precede obj2 in the
     * result ("o1 < o2").
     */
    public static <T> List<T> sort(MultiMap<T,T> pMap) throws CycleExistsException {
        DirectedGraph<T> g = new DirectedGraph<T>(pMap);
        return sort(g);        
    }
    
    /**
     * Make a total ordering out of a partial ordering.
     * @param pPairs a list of pairs, where obj1 should precede obj2 in the
     * result ("o1 < o2").
     */    
    public static <T> List<T> sort(DirectedGraph<T> g) throws CycleExistsException{       
        
        Logger.info("Sorting graph:\n"+g);
        
        List<T> result = new ArrayList<T>();
        
        Set<T> objects = new HashSet();
        objects.addAll(g.getSrcNodes());
        objects.addAll(g.getTgtNodes());
        
        ArrayStack<T> C = new ArrayStack();
        for (T o: objects) {
            if (g.inDegree(o) == 0) {
                // no precedessors
                C.add(o);
            }
        }
        
        while (C.size() > 0) {
            T i = C.pop();
            result.add(i);
            for (T j: new ArrayList<T>(g.followers(i))) {
                g.removeEdge(i,j);
                if (g.inDegree(j) == 0) {
                    // in-degree became 0
                    C.push(j);
                }                               
            }                       
        }
        
        if (result.size() == objects.size()) {
            return result; 
        }
        
        else {
            throw new CycleExistsException();
        }
        
        
    }
    
    public static class CycleExistsException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3802983864763338934L;
        
    }
    
    public static void main(String[] args) {
        try {
            List<SymmetricPair<String>> pairs = IOUtils.readPairs(System.in);
//            Logger.info("Read pairs:\n"+StringUtils.collectionToString(pairs));
            List<String> ordered = sort(pairs);
            for (String s: ordered) {
                System.out.println(s);
            }
        }
        catch (IOException e){
            Utils.die(e);
        }
        catch (CycleExistsException e){
            Utils.die("A cycle exists in the input graph!");
        }
        
    }
        
}
