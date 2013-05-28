package util.matrix;

import util.converter.*;

import java.util.*;

/** The official way to make Rows. */
public interface RowFactory extends Converter<String,List> {
    
    public List makeRow();
    public List makeRow(String pDataString);
    public List makeRow(List pRowToClone);
    public void setSeparator(String separatorRegex);
    public String getSeparator();
                  
}
