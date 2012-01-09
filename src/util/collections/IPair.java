package util.collections;

import java.util.*;

/** A pair; also qualifies as a list with two elements */ 
public interface IPair<T1, T2> extends List {

    public T1 getObj1();
    public T2 getObj2();
    
}
