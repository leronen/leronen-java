package util.converter;

import util.Initializable;


public class PrependConverter implements Converter<String, String>, Initializable {
        
    String mPrefix = "foo";
    
    public void init(String p) {
        mPrefix = p;
    }
    
    public String convert(String p) {
        return mPrefix + p;
       
    }
}
