package util;


/**
 * Database-independent column types. Presently not aimed to be in anyway comprehensive,
 * the main purpose is to differentiate between text, real and integer types.
 */
public enum ColumnType {
    STRING,
    INTEGER,
    FLOAT,    
    UNKNOWN;
    
    /** @return true if value is acceptable for column of this type, false if not */
    public boolean checkValue(String val) {
        switch(this) {
            case STRING:
                return true;      // anything goes                
            case FLOAT:
                return StringUtils.isNumber(val);                
            case INTEGER:
                return StringUtils.isInteger(val);                
            case UNKNOWN:
                throw new RuntimeException("Cannot check value of column type UNKNOWN");                
        }
        
        throw new RuntimeException("Not reached");
    }
}
