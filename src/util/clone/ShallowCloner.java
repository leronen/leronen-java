package util.clone;

public class ShallowCloner<T> implements Cloner<T> {
            
    public T createClone(T pObj) {        
        return pObj;
    }
    

}
