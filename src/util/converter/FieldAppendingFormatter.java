package util.converter;




/** 
 * Converts an object to string using toString, with the additional 
 * finesse of appending the value of a field in paranthesis, the field value
 * being extracted by a dedicated  converter. The field value is also gracefully 
 * formatted using toString...
 */
public final class FieldAppendingFormatter implements Converter {         
        
    private Converter mFieldExtractor;
    
    public FieldAppendingFormatter(Converter pFieldExtractor) {
        mFieldExtractor = pFieldExtractor;         
    }
    
    public String convert(Object p) {
        String field = mFieldExtractor.convert(p).toString();
        return p.toString() +" ("+field +")";    
    }
}
