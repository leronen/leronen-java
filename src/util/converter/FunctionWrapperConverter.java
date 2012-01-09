package util.converter;

import util.collections.Function;

/** 
 * Converts List to Pairs. Note that the lists to be converted must have exactly 2 elements, or else!
 */
public final class FunctionWrapperConverter<T1, T2> implements Converter<T1, T2> {    
       
    private Function<T1,T2> mFunction;       
       
    public FunctionWrapperConverter(Function<T1,T2> pFunc) {
        mFunction = pFunc;               
    }
    
    // write new constructors of the same flavor at will...
       
    public T2 convert(T1 pObj) {
        return mFunction.compute(pObj);            
    }
}
