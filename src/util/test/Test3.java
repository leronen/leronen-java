package util.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import util.Timer;

public class Test3 {
    
    public static void main(String[] args) throws Exception {
        LinkedList ll = new LinkedList();
        ArrayList al = new ArrayList();
        Vector vec = new Vector();
        int NUM_ELEMENTS = 1000000;
        
        Timer.startTiming("arraylist");
        for (int i=0; i<NUM_ELEMENTS; i++) {
            al.add(i);
        }
        Timer.endTiming("arraylist");
        
        Timer.startTiming("vector");
        for (int i=0; i<NUM_ELEMENTS; i++) {
            vec.add(i);
        }
        Timer.endTiming("vector");
        
        Timer.startTiming("linkedlist");
        for (int i=0; i<NUM_ELEMENTS; i++) {
            ll.add(i);            
        }
        Timer.endTiming("linkedlist");
        
        
        Timer.startTiming("linkedlist remove");
        for (int i=0; i<NUM_ELEMENTS; i++) {
            ll.removeLast();
        }
        Timer.endTiming("linkedlist remove");
        
        Timer.startTiming("arraylist remove");
        for (int i=0; i<NUM_ELEMENTS; i++) {
            al.remove(al.size()-1);
        }
        Timer.endTiming("arraylist remove");

        Timer.logToStdErr();
        
//        List<String> l = new ArrayList();
//        l.add("A");
//        l.add("B");
//        l.add(null);
//        l.add("D");
//        System.out.println(l);
    }
        
}
