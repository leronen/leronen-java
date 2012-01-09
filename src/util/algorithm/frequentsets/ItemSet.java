package util.algorithm.frequentsets;

import java.util.HashSet;
import java.util.Set;

public class ItemSet extends HashSet<Item> {
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
