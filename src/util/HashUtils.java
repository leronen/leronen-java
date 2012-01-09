package util;

import java.lang.reflect.Array;

	
/**
 *  Collected methods which allow easy implementation of <code>hashCode</code>.
 *
 *  Copied from the internet by leronen.
 *  
 *  Example use case:
 *  <pre>
 *  public int hashCode(){
 *     int result = HashUtils.SEED;
 *    //collect the contributions of various fields
 *    result = HashUtils.hash(result, fPrimitive);
 *    result = HashUtils.hash(result, fObject);
 *    result = HashUtils.hash(result, fArray);
 *    return result;
 *  }
 * </pre>
 */
public class HashUtils {

	/**
	* An initial value for a <code>hashCode</code>, to which is added contributions
	* from fields. Using a non-zero value decreases collisons of <code>hashCode</code>
	* values.
	*/
	public static final int SEED = 23;
	
	/**
	* booleans.
	*/
	public static int hash( int aSeed, boolean aBoolean ) {	    
	    return firstTerm( aSeed ) + ( aBoolean ? 1 : 0 );
	}

   /**
	* chars.
	*/
	public static int hash( int aSeed, char aChar ) {	    
	    return firstTerm( aSeed ) + aChar;
	}

   /**
	* ints.
	*/
	public static int hash( int aSeed , int aInt ) {
	  
	  // Note that byte and short are also handled by this method, through
	  // implicit conversion.
	  
	    
	    return firstTerm( aSeed ) + aInt;
	}

   /**
	* longs.
	*/
	public static int hash( int aSeed , long aLong ) {	    
	    return firstTerm(aSeed)  + (int)( aLong ^ (aLong >>> 32) );
	}

   /**
	* floats.
	*/
	public static int hash( int aSeed , float aFloat ) {
	    return hash( aSeed, Float.floatToIntBits(aFloat) );
	}

   /**
	* doubles.
	*/
	public static int hash( int aSeed , double aDouble ) {
	    return hash( aSeed, Double.doubleToLongBits(aDouble) );
	}

   /**
	* <code>aObject</code> is a possibly-null object field, and possibly an array.
	*
	* If <code>aObject</code> is an array, then each element may be a primitive
	* or a possibly-null object.
	*/
    public static int hash( int aSeed , Object aObject ) {
	    int result = aSeed;
	    if ( aObject == null) {
	        result = hash(result, 0);
	    }
	    else if ( ! isArray(aObject) ) {
	        result = hash(result, aObject.hashCode());
	    }
	    else {
	        int length = Array.getLength(aObject);
	        for (int idx = 0; idx < length; ++idx ) {
	            Object item = Array.get(aObject, idx);	            
	            result = hash(result, item);
	        }
	    }
	    return result;
    }
    
   /**
    * Added this myself to ensure consistent hashing of char sequences 
    * independent of implementation.
    */
    public static int hash( int aSeed , CharSequence aSeq ) {
        int result = aSeed;
        if ( aSeq == null) {
            result = hash(result, 0);
        }        
        else {
            int length = aSeq.length();
            for (int i = 0; i < length; ++i ) {
                char c = aSeq.charAt(i);              
                result = hash(result, c);
            }
        }
        return result;
    }

	/// PRIVATE ///
	private static final int fODD_PRIME_NUMBER = 37;

	private static int firstTerm( int aSeed ){
	    return fODD_PRIME_NUMBER * aSeed;
	}

	private static boolean isArray(Object aObject){
	    return aObject.getClass().isArray();
	}
	
	public static void main(String[] args) {
		System.out.println(args[0].hashCode());
	}
} 