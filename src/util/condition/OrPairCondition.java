package util.condition;

import util.collections.*;

public class OrPairCondition implements Condition {
    private Condition mCondition;    
    
    public OrPairCondition(Condition pCondition) {                        
        mCondition = pCondition;        
    }
    
    
    public boolean fulfills(Object pObj) {
    	// KLUUUDDDDDGGEEEEEGEGE!
    	if (pObj instanceof SymmetricPair) {
    		SymmetricPair pair = (SymmetricPair)pObj;
    		return mCondition.fulfills(pair.getObj1()) || 
    			   mCondition.fulfills(pair.getObj2()); 
 		}
    	else if (pObj instanceof IPair) { 
    		IPair pair = (IPair)pObj;
    		return mCondition.fulfills(pair.getObj1()) || 
                   mCondition.fulfills(pair.getObj2());
    	}
    	else {
    		throw new RuntimeException("Cannot handle objects of class: "+pObj.getClass());
    	}
    }        
}
