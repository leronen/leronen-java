package util.factory;

import java.util.*;

public class TreeSetFactory implements ParametrizedFactory<Set, Integer> {

	public Set makeObject() {
		return new TreeSet();
	}
    
    /**
     * Let's not support this... (we have to implement ParametrizedFactory
     * to keep MultiMap happy...
     */        
    public Set makeObject(Integer pParam) {
        throw new UnsupportedOperationException();
    }

}
