package util.matrix;

import util.converter.*;

import java.util.*;

/** The official way to make Rows. */
public interface RowFactory extends Converter {
    
    public List makeRow();
    public List makeRow(String pDataString);
    public List makeRow(List pRowToClone);        
                  
}