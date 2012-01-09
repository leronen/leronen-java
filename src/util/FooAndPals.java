package util;

import java.util.ArrayList;
import java.util.List;

public enum FooAndPals {
	FOO,
	BAR,
	BAZ,
	QUX,
	QUUX,
	GARPLY,
	WALDO,
	FRED,
	BLUGH,
	XYZZY,
	THUD;
    
    public static void main(String[] args) {
//        PriorityQueue<String> foo = null;
        
        
	    for (FooAndPals f: FooAndPals.values()) {
	        System.out.println(f);
        }
    }
    
    public static List<String> asStringList() {
        List<String> result = new ArrayList<String>();
        
        for (FooAndPals val: FooAndPals.values()) {
            result.add(val.toString());
        }
        
        return result;
    }
}