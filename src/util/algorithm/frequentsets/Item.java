package util.algorithm.frequentsets;

import java.util.ArrayList;
import java.util.List;

/** 
 * There should never exist any duplicate instances, as enforced by ItemManager;
 * that is, keys and indices are unique, and have a one-to-one mapping.
 */
public class Item {
    
    public String key;
    public int ind;
    public List<ItemSet> is_contained_in = new ArrayList();
       
    public Item(String pKey, int pInd) {
        this.key = pKey;
        this.ind = pInd;
    }
    
    public String toString() {
        return key;
        // return key +"("+ind+")";
    }
        
        
}
