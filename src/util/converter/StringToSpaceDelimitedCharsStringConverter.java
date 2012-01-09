package util.converter;

import util.*;
import java.util.*;

public final class StringToSpaceDelimitedCharsStringConverter implements Converter {         
    
    public Object convert(Object pObj) {
        String s = (String)pObj;
        List chars = ConversionUtils.asList(s);
        return StringUtils.listToString(chars, " ");            
    }
}
