package util.condition;

import util.converter.*;

/** Condition that tests whether the object, as converter by a converter, matches the given condition */
public class ConvertedObjectCondition implements Condition {
    private Converter mConverter;
    private Condition mBaseCondition;
    
    public ConvertedObjectCondition(Converter pConverter, Condition pBaseCondition) {
        mConverter = pConverter;
        mBaseCondition = pBaseCondition;
    }
    
    public boolean fulfills(Object pObj) {        
        return mBaseCondition.fulfills(mConverter.convert(pObj));    
    }        
    
}
