package util.algorithm.frequentsets;

import java.util.HashSet;
import java.util.Set;

public class ItemSet extends HashSet<Item> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8816721323650754939L;
	int item_count = 0;
    int freq_count = 0;
    
    public ItemSet(int pInitialCapacity) {
        super(pInitialCapacity);
    }
    
    public ItemSet(Set<Item> pItems) {
        super(pItems.size());
        addAll(pItems);
    }
}
