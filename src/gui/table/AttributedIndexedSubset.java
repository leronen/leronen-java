package gui.table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import util.collections.OneToOneBidirectionalMap;

public class AttributedIndexedSubset<T> extends IndexedSubset<T> {

    private Map<String, Object> mAttributes;
    
    public AttributedIndexedSubset(Collection<T> pSubSet, 
                                   OneToOneBidirectionalMap<Integer, T> pSuperSetMap) {
        super(pSubSet, pSuperSetMap, null);
        mAttributes = new HashMap<String, Object>();
}

    public AttributedIndexedSubset(Collection<T> pSubSet, 
                                   OneToOneBidirectionalMap<Integer, T> pSuperSetMap,
                                   Double pWeight) {
        super(pSubSet, pSuperSetMap, pWeight);
        mAttributes = new HashMap();
    }
    
    public Object get(String pKey) {
        return mAttributes.get(pKey);
    }
    
    public void put(String pKey, Object pVal) {
        mAttributes.put(pKey, pVal);
    }
    
}
