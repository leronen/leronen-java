package util.converter;


import java.util.*;

/** 
 * Converts indices (Integer instances) to the corresponding objects in list.
 * 
 * Philosophy: a list can be interpreted as a function (non-negative-integers) -> (any))
 *
 * Todo: somewhere in time: rename Converter to Function.     
 */
public final class IndexToListElementConverter implements Converter {

    private List mList;    

    public IndexToListElementConverter(List pList) {
        mList = pList;
    }         
    
    public Object convert(Object pObj) {
        int index = ((Integer)pObj).intValue();
        // dbgMsg("index: "+index);
        return mList.get(index);                                           
    }
    
    
}
