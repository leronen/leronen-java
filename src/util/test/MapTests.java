package util.test;

import java.util.HashMap;
import java.util.Map;

import util.StringUtils;

public class MapTests {
    
    public static void main(String[] args) {
        Map m = new HashMap();
        
        m.put("foo", "bar");
        m.put("baz", null);
        
        System.out.println(StringUtils.mapToString(m));
        System.out.println("Val of baz: "+m.get("baz"));
        System.out.println("Contains key baz: "+m.containsKey("baz"));
    }
}
