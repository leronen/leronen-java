package util.converter;

/** 
 * Interface that enables making strings from objects; note that this extends Converter, so an from-Object conversion 
 * functionality must also be available as a wrapper for makeFromString()
 */
public interface StringBasedFactory extends Converter {   
    public Object makeFromString(String pString);
}

