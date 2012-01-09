package util.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ConversionUtils;
import util.converter.ObjectToStringConverter;

public class EnumTest {
    
    public static void main(String[] args) {
        String name = args[0];
        SomeEnum e = SomeEnum.getByName(name);
        if (e != null) {
            System.err.println("Got enum: "+e);
            switch (e) {
            case FOO:
                System.err.println("It is FOO");
                break;
            case BAR:
                System.err.println("It is BAR");
                break;
            case BAZ:
                System.err.println("It is BAZ");
                break;
            default:
                throw new RuntimeException("No case for enum: "+e+"; this is actually an unrecoverable coding error!");                
            }
        }
        else {
            System.err.println("No such enum: "+e);
        }
    }
    
    public enum SomeEnum {
        FOO("foo"),
        BAR("bar"),        
        BAZ("baz");
                
        String mName;
        
        public static Map<String, SomeEnum> BY_NAME;
        
        static {
            BY_NAME = new HashMap();
            for (SomeEnum od: values()) {
                BY_NAME.put(od.mName, od);
            }
        }
        
        /** Return null, if no such enum */
        public static SomeEnum getByName(String pName) { 
            return BY_NAME.get(pName);     
        }
                                               
        private SomeEnum(String pName) {                         
            mName = pName;            
        }

        public String toString() {
            return mName;
        }

        public static List<String> names() {
            return ConversionUtils.convert(Arrays.asList(SomeEnum.values()),
                   new ObjectToStringConverter());
        }
    }
}
