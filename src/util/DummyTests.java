package util;
import util.dbg.*;

class DummyTests {
    public static void main (String[] args) {
        testNan();
    }    
    
    public static void testNan() {
        double a = Double.NaN;
        double b = 0.d/0.d;
        double c = Double.POSITIVE_INFINITY/Double.POSITIVE_INFINITY;
        dbgMsg("a: "+a+" "+(a==Double.NaN));
        dbgMsg("b: "+b+" "+(b==Double.NaN));
        dbgMsg("c: "+c+" "+(c==Double.NaN));
        
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("DummyTests: "+pMsg);
    }
    
}
