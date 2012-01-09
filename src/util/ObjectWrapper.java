package util;

/**
 * For implementing something like "variable params", in the honorable tradition
 * of pascal, or c-pointers.
 */
public class ObjectWrapper<T> {
    
    public T obj = null;

    /** 
     * Note that we generally do not want the "pointer" to the wrapped object to 
     * be initialized just yet; that is why only this no-op constructor is provided. 
     */
    public ObjectWrapper() {
        
    }
    
//    public ObjectWrapper(T pObj) {  
//        this.obj = pObj;
//    }

}
