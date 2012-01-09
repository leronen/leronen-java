package util.comparator;


import util.converter.*;

/**
 * Comparator, which orders objects by comparing one specific field of the objects, instead of the objects 
 * themselves. The field is specified through an helper object that extracts the ordering field from the actual objects.
 */
public class ByStringComparator extends ByFieldComparator {                             

    /** The ordering imposed by pBaseComparator provided onto the domain mapped by pFieldExtractor */
    public ByStringComparator() {
        super(new ObjectToStringConverter());        
    }
    
    
}
