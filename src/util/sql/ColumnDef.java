package util.sql;

import util.converter.Converter;

public class ColumnDef {

    private String mName;
    private Type mType;
    
    public ColumnDef(String pName, Type pType) {
        mName = pName;
        mType = pType;        
    }
    
    public enum Type {
        STRING, // implemented as String
        INT,    // implemented as Integer
        DECIMAL // implemented as Double
    }    
    
    public String getName() {
        return mName;
    }
    
    public Type getType() {
        return mType;
    }
    
    public static class NameExtractor implements Converter<ColumnDef, String> {
        public String convert(ColumnDef pColDef) {
            return pColDef.mName;
        }
    }
    
}
