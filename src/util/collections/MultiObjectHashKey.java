package util.collections;

import java.util.*;

public class MultiObjectHashKey extends ArrayList {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4701574474457096842L;

	public MultiObjectHashKey(Object pObj1, Object pObj2) {
        super(2);
        add(pObj1);
        add(pObj2);
    }

}

