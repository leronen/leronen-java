package util.test;

/** Another test for Fang */
public class Test2 {
    
    public static void testMethod(A p) {
        if (p instanceof B) {
            B b = (B)p;
            b.bMethod();
        }
    }
                                            
}

abstract class A {
    public abstract void doSomething(String p);
}

class B extends A {
    public void doSomething(String p) {
        //
    }
    
    public void bMethod() {
        // 
    }
}

class C extends A {
    public void doSomething(String p) {
        //
    }
}

class D extends A {
    public void doSomething(String p) {
        //
    }
}





