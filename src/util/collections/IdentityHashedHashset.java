package util.collections;

import java.util.Collection;
import java.util.HashSet;

import util.StringUtils;

/** 
 * A hashset whose equality is based on object identity.
 * The identities of objects IN the set is based on hashCode and equals
 * of the objects, as usual; it is just the set itself that is affected
 * by this!
 */
public class IdentityHashedHashset<T> extends HashSet<T> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 8094059959181164064L;


	public IdentityHashedHashset() {
        super();
    }
    
    public IdentityHashedHashset(Collection<T> pData) {
        super(pData);
    }
    
    public boolean equals(Object p) {
        return this == p;
    }
    
    public int hashCode() {
        return System.identityHashCode(this);
    }
        
    
    public String toString() {
        return "IdentityHashedHashMap: "+StringUtils.collectionToString(this, ", ");
    }
}
