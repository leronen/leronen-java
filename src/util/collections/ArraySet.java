package util.collections;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import util.dbg.Logger;


/**
 * A kluudgy Set implementation, done in hopes to get around the horrible
 * performance overhead of HashSets. 
 * 
 * Contains many possibilities for error, I'm afraid.
 * 
 * Allow adding elements only when constructing.
 * Warn about inefficient add and remove operations.
 *    
 */
public class ArraySet<E>
    extends ArrayList<E>
    implements Set<E>
{
       
    /** Only allow adding elements when constructing */
    boolean addsEnabled = true;
    
    /** The sole constructor to date. */
    public ArraySet(Collection<? extends E> c)
    {
        super(c);
        addsEnabled = false;
    }
        
    public boolean add(E e) {
        if (!addsEnabled) {
            throw new RuntimeException("Adds not enabled");
        }
        else {
            return super.add(e);
        }
    }
    
    public boolean addAll(Collection<? extends E> c) {
        if (!addsEnabled) {
            throw new RuntimeException("Adds not enabled");
        }
        else {
            return super.addAll(c);
        }
    }
    
    public E remove() {
        throw new UnsupportedOperationException();
    }
    
    public boolean contains(Object o) {
        Logger.warning("Inefficient operation ArraySet.contains() called");
        return super.contains(o);
    }
    
    //~ Constructors -----------------------------------------------------------
        
    
    //~ Methods ----------------------------------------------------------------

    public boolean equals(Object o)
    {
        Logger.warning("Calling ArrayUnenforcedSet.equals(), which is a very " +
                       "costly operation indeed");
        return new SetForEquality().equals(o);
    }

    public int hashCode()
    {
        Logger.warning("Calling ArrayUnenforcedSet.hashCode(), which is a very " +
                       "costly operation indeed");
        return new SetForEquality().hashCode();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Multiple inheritance helper.
     */
    private class SetForEquality
        extends AbstractSet<E>
    {
        public Iterator<E> iterator()
        {
            return ArraySet.this.iterator();
        }

        public int size()
        {
            return ArraySet.this.size();
        }
    }
    
    /**
     * Return false if the ArraySet contains duplicate elements.
     * Implemented by constructing an HashSet*/
    public boolean check_hash() {
        HashSet<E> realSet = new HashSet(this);
        return realSet.size() == this.size();
    }
    
    public boolean check_comparison() {
        TreeSet<E> realSet = new TreeSet(this);
        return realSet.size() == this.size();
    }
}

// End ArrayUnenforcedSet.java
