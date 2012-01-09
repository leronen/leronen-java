package util.test;

import java.util.ArrayList;
import java.util.List;

public class GenericsTests {
    
    public static void main(String[] args) throws Exception {
        new GenericsTests().test1();
    }
    
    public void test1() {
        List<Dummy> l1 = new ArrayList();
        l1.add(new Dummy());
        
        List<Dummy> l2 = new ArrayList();
        l2.add(new Dummy2());
        
        @SuppressWarnings("unused")
        List<? extends Dummy> l3 = new ArrayList();
        // Note that nothing can be added to a ?-parametrized ArrayList,
        // as it cannot be guaranteed that the actual object is of
        // the same type as l3; on the other hand, the previous case (2)
        // works, as it is possible to add a subclass instance to a
        // a list of superclass type
        // l3.add(new Dummy());
        
    }
    
    public class Dummy {
        // foo
    }
    
    public class Dummy2 extends Dummy {
        // bar
    }
}
