package util;

import java.util.Arrays;
import java.util.Collection;

public class OptDef {
    
    public String longname;
    public String shortname;
    public boolean hasvalue;
    public String defaultvalue;
    public Collection<String> possiblevalues;
    
    public static OptDef HELP = new OptDef(
        "help",
        "h",
        false);        
       
    public OptDef(String pLongName,
                  String pShortName,
                  boolean pHasValue,
                  String pDefaultValue,
                  Collection<String> pPossibleValues) {
        longname = pLongName;   
        shortname = pShortName;
        hasvalue = pHasValue;        
        defaultvalue = pDefaultValue;        
        possiblevalues = pPossibleValues;        
    }
    
    public OptDef(Object... pData) {                   
        longname = (String)pData[0];        
        shortname = (String)pData[1];
        hasvalue = ((Boolean)pData[2]).booleanValue();        
        defaultvalue = (pData.length >= 4 && pData[3] != null) ? ""+pData[3] : null;        
        possiblevalues = null;
        if (pData.length == 5) {
            if (pData[4] instanceof String[]) {            
                possiblevalues = Arrays.asList((String[])pData[4]);
            }
            else if (pData[4] instanceof Collection) {
                possiblevalues = (Collection)pData[4];
            }
            else {
                throw new RuntimeException("Unrecognized enum def: "+pData[4]);
            }
        }
        
        if (pData.length > 5) {
            throw new RuntimeException("Too long arg def list!");
        }
    }
}
