package util.factory;

import java.util.*;

import util.comparator.ReverseComparator;

public class TreeMapFactory<K,V> implements ParametrizedFactory<Map<K,V>, Integer> {

    private boolean mReverseOrder;
    
    public TreeMapFactory() {
        mReverseOrder = false;        
    }
    
    public TreeMapFactory(boolean pReverseOrder) {
        mReverseOrder = pReverseOrder;        
    }
    
	public Map makeObject() {
        if (mReverseOrder) {
            return new TreeMap(new ReverseComparator());
        }
        else {
            return new TreeMap();
        }
		
	}
    
    /**
     * A nuisance method in TreeMapFactory, as a treemap does not have a fixed size.
     * Let's throw a RuntimeException...
     */ 
    public Map<K,V> makeObject(Integer pSize) {
        throw new UnsupportedOperationException();
        // return makeObject();
    }

}
