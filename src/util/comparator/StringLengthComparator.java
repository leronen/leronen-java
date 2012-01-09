package util.comparator;


import java.util.Comparator;


/**
 * Comparator, which orders objects by comparing one specific field of the objects, instead of the objects 
 * themselves. The field is specified through an helper object that extracts the ordering field from the actual objects.
 */
public class StringLengthComparator implements Comparator<String> {

    @Override
    public int compare(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return arg0.length() - arg1.length();
    }

    
    
    
}
