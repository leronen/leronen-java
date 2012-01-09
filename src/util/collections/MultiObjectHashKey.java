package util.collections;

import java.util.*;

public class MultiObjectHashKey extends ArrayList {

    public MultiObjectHashKey(Object pObj1, Object pObj2) {
        super(2);
        add(pObj1);
        add(pObj2);
    }

}

