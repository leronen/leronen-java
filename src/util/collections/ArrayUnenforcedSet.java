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
 * An "unenforced" set implementation, to be used when a set is required, 
 * but we actually do not need full set capabilities,
 * but know in advance that our collection at hand is indeed a set.
 * (this has been stolen from the jgrapht library (package orgt.jgrapht.util),
 *  which is not to say that implementing something like this would not
 *  be a trivial task.    
 * 
 * <begin original doc>
 * Helper for efficiently representing small sets whose elements are known to be
 * unique by construction, implying we don't need to enforce the uniqueness
 * property in the data structure itself. Use with caution.
 * 
 *
 * <p>Note that for equals/hashCode, the class implements the Set behavior
 * (unordered), not the list behavior (ordered); the fact that it subclasses
 * ArrayList should be considered an implementation detail.
 * 
 * @author John V. Sichi
 * 
 * <end original doc, begin my own humble doc>
 * 
 * Note that this can actually be useful for large sets also, but in that case 
 * containment checking will be hugely inefficient (linear search in array), 
 * so it has to be ensured that such actions shall not take place.
 * 
 * Also note, that equals and hashcode are very unefficiently implemented, 
 * indeed; I now added some warnings to them, in the unfortunate case
 * that they should accidentally get called... 
 *
 * Also got rid of the conventional serialVersionUID...
 * 
 * Here we also learn the lesson that we can implement an interface using
 * methods from a superclass, provided that the superclass has methods matching
 * the signature of the interface (e.g. here we get all methods for set from
 * an arraylist, which does not explicitly implement Set); OK, we actually
 * learn a second lesson, which implies we might have to immediately unlearn
 * the first lesson just learnt: java's Set interface does not seem to add
 * anything to the Collection interface. What's more, all Collection methods
 * are duplicated in Set, probably just to enable writing appropriate javadoc
 * comments for them. 
 *    
 */
public class ArrayUnenforcedSet<E>
    extends ArrayList<E>
    implements Set<E>
{

    //~ Constructors -----------------------------------------------------------

    public ArrayUnenforcedSet()
    {
        super();
    }
    /**
     * Construct set with 2 elements (for 1 element, vi recommenderar 
     * Collections.singleton() 
     */
    public ArrayUnenforcedSet(E e1, E e2)
    {
        super();
        add(e1);
        add(e2);
    }
    
    /**
     * Construct set with 3 elements (implementation for the outrageous 4- or 
     * more element-constructors pending...)
     */
    public ArrayUnenforcedSet(E e1, E e2, E e3)
    {
        super();
        add(e1);
        add(e2);
        add(e3);
    }
    
    public ArrayUnenforcedSet(Collection<? extends E> c)
    {
        super(c);
    }

    public ArrayUnenforcedSet(int n)
    {
        super(n);
    }

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
            return ArrayUnenforcedSet.this.iterator();
        }

        public int size()
        {
            return ArrayUnenforcedSet.this.size();
        }
    }
    
    /**
     * Return false if the ArrayUnenforcedSet contains duplicate elements.
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
