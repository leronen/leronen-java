package util.converter;

/** 
 * Converts List to Pairs. Note that the lists to be converted must have exactly 2 elements, or else!
 */
public final class ConverterChain<T1, T2> implements Converter<T1, T2> {    
       
    private Converter mConverters[];       
       
    public ConverterChain(Converter pConverter1, Converter pConverter2) {
        mConverters = new Converter[2];
        mConverters [0] = pConverter1;
        mConverters [1] = pConverter2;        
    }
    
    public ConverterChain(Converter pConverter1, Converter pConverter2, Converter pConverter3) {
        mConverters = new Converter[3];
        mConverters [0] = pConverter1;
        mConverters [1] = pConverter2;
        mConverters [2] = pConverter3;                
    }
    
    // write new constructors of the same flavor at will...
       
    public T2 convert(T1 pObj) {
        Object current = pObj;
        for (int i=0; i<mConverters.length; i++) {
            current = mConverters[i].convert(current);    
        }
        return (T2)current;    
    }
}
