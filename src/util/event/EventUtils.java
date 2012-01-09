package util.event;

import java.util.*;

import util.*;
import util.collections.*;

public final class EventUtils {

    public static void fireDataChanged(Object pSource, Collection pListeners) {
        CollectionUtils.forEach(pListeners, new FireDataChangedOperation(pSource));                                     
    }
    
    public static void fireItemRenamed(Class pItemClass, Object pOldId, Object pNewId, Collection pListeners) {
        CollectionUtils.forEach(pListeners, new FireItemRenamedOperation(pItemClass, pOldId, pNewId));
    }                
    
    private static class FireDataChangedOperation implements Operation {
        private Object mSource;
        
        private FireDataChangedOperation(Object pSource) {
            mSource = pSource;
        }                    
        
        public void doOperation(Object pObj) {
            ModelListener listener = (ModelListener)pObj;
            listener.dataChanged(mSource);
        }
    }
    
    private static class FireItemRenamedOperation implements Operation {        
        private Class mItemClass;
        private Object mOldId;
        private Object mNewId;
        
        private FireItemRenamedOperation(Class pItemClass,
                                        Object pOldId,
                                        Object pNewId) {
            mItemClass = pItemClass;
            mOldId = pOldId;
            mNewId = pNewId;                                    
        }                                                            
        
        public void doOperation(Object pObj) {
            ModelListener listener = (ModelListener)pObj;
            listener.itemRenamed(mItemClass, mOldId, mNewId);
        }
        
    }
}
