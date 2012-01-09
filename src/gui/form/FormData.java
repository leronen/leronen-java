package gui.form;

import java.util.Map;
import java.util.Set;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

import util.CollectionUtils;

public interface FormData { 
    public static final String TYPE_STRING = "string";
    public static final String TYPE_INT = "int";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_ENUM = "enum";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_FILE = "file";
    
    public static Set<String> NUMERIC_TYPES = CollectionUtils.makeHashSet(
            TYPE_INT, TYPE_FLOAT);
    
    public static final String DEFAULT_VAL_STRING = "<default>";
    
    public Object get(String pKey);
    public Object put(String pKey, Object pVal);
    
    public Object getDefaultVal(String pKey);
    
    public boolean isEditable(String pKey);
    public String getFormName();
    public String getType(String pKey);
    public boolean isNumeric(String pKey);
    public Object[] getOptions(String pKey);    
    public String[] getAllKeys();
    // public Set getObligatoryFields();
    /**
     * Implementation can freely throw UnsupportedOperationException in the case that it is an read-only-form 
     * Stream is NOT to be closed by the implementation, that is up to the caller
     * (he/she might still want to use the stream for other purposes...)
     */
    public void writeToStream(PrintWriter pWriter) throws IOException;
    public boolean isReadOnly();
    // may return null
    public String getTooltip(String pKey);
    
    /** Form data may want to provide it's own document... just return null, if don't feel like it*/
    public Document getDocumentForTextField(String pKey);    
    /** Form data may want to provide it's own document... just return null, if don't feel like it*/
    public ComboBoxModel getDocumentForComboBox(String pKey);
    
    public String getDependentField(String pFieldToDependOn);
    public void updateDependentField(String pFieldName);
    
    public Map<String, Object> asMap();
}
