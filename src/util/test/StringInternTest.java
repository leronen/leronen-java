package util.test;

import util.dbg.Logger;

public class StringInternTest {

    public static void main(String[] args) throws Exception {
        String foo = "foo".intern();
        String Foo = "Foo".intern();
        
        Logger.info("foo:"+foo);
        Logger.info("Foo:"+Foo);
    }
}
