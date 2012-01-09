package gui.form;

import util.CollectionUtils;
import util.Timer;
import util.converter.*;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

public abstract class AbstractFormData implements FormData {
    
    protected static final Integer DEFAULT_INT = new Integer(0);
    protected static final String DEFAULT_STRING = "";
    protected static final Boolean DEFAULT_BOOLEAN = Boolean.TRUE;
    protected static final Float DEFAULT_FLOAT = new Float(0);
    protected static final File DEFAULT_FILE = new File("~");
    
    public void writeToStream(PrintWriter pWriter) throws IOException {        
        // dbgMsg("writeToStream");
        String[] allKeys = getAllKeys();        
        for (int i=0; i<allKeys.length; i++) {
            String key = allKeys[i];
            if (excludeFromSerialization(key)) {
                continue;    
            }
            String valString = FormData.DEFAULT_VAL_STRING;
            Object val = get(key); 
            if (val!=null) {                                
                valString = val.toString();
            }
            String line = key+"="+valString;
            // dbgMsg("writing line: "+line);
            pWriter.println(line);    
        }
    }        
    
    public void writeToFile(File pFile) throws IOException {
        FileWriter fw = new FileWriter(pFile);
        PrintWriter pw = new PrintWriter(fw);
        writeToStream(pw);
        pw.close();
        fw.close();     
    }
    
    
    public void readFromFile(String pFileName) throws IOException {
        Timer.startTiming("Read summary from file");
        FileInputStream fis = new FileInputStream(pFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();
        Set<String> readParams = new HashSet();
        // Pattern p =  Pattern.compile("^(\\w+)=((\\w|\\.)+)$");
        // Pattern p =  Pattern.compile("^(\\w+)=(.*)$");
//        Pattern p =  Pattern.compile("^([^=]+)=(.*)$");
        while(line!=null) {            
//            Matcher m = p.matcher(line);
//            if (m.matches()) {
            int i = line.indexOf('=');
            if (i != -1) {
                String name = line.substring(0, i);
                String val = line.substring(i+1);
//                String param = m.group(1);
//                String val = m.group(2);
                
                // dbgMsg("param = "+param+", val="+val);
                put(name, val);
                readParams.add(name);
            }
            line = reader.readLine();    
        }          
        
        // patch missing keys!
        Set<String> unreadParams = CollectionUtils.minus(getAllKeysSet(), readParams);
        for (String key: unreadParams) {
            put(key, getDefaultVal(key));
        }
        
        reader.close();
        
        Timer.endTiming("Read summary from file");
    }
        
                    
    
    /** Subclasses may return true in order to prevent fields from being saved */    
    public boolean excludeFromSerialization(String pKey) {
        return false;
    }

    /** Normally forms are not read-only */
    public boolean isReadOnly() {
        return false;
    }
    
    /** By default all fields are editable */
    public boolean isEditable(String pKey) {
        return true;    
    }
    
    public String getType(String pKey) {            
        return FormData.TYPE_STRING;                                                                           
    }
    
    public boolean isNumeric(String pKey) {
        String type = getType(pKey);
        return FormData.NUMERIC_TYPES.contains(type);        
    }

    /** By default, return null, in which case no name should be shown */
    public String getFormName() {
        return null;
    }        
    
    /** Return null, if no such key */
    public Object getDefaultVal(String pKey) {
                       
        String type = getType(pKey);
        
        if (type == null) {
            // no such key
            return null;
        }
        
        if (type.equals(TYPE_STRING)) {
            return DEFAULT_STRING;
        }
        else if (type.equals(TYPE_INT)) {
            return DEFAULT_INT;
        }
        else if (type.equals(TYPE_FLOAT)) {
            return DEFAULT_FLOAT;
        }
        else if (type.equals(TYPE_ENUM)) {
            ComboBoxModel comboModel = getDocumentForComboBox(pKey);
            if (comboModel != null) {
                if (comboModel.getSize() > 0) {
                    return comboModel.getElementAt(0);
                }
                else {
                    return null;
                }
            }
            else {
                return getOptions(pKey)[0];
            }
        }
        else if (type.equals(TYPE_BOOLEAN)) {
            return DEFAULT_BOOLEAN;
        }
        else if (type.equals(TYPE_FILE)) {
            return DEFAULT_FILE;
        }
        else {
            throw new RuntimeException("Unknown type for field ("+pKey+"): "+type);
        }
    }
    
    public Object[] getOptions(String pKey) {
        throw new RuntimeException("Not implemented!");
    }
    
    /** Just a convenience */
    public Set<String> getAllKeysSet() {
        return new HashSet<String>(Arrays.asList(getAllKeys()));
    }
    
    public void setDefaultVals() {
        String[] keys = getAllKeys();
        for (int i=0; i<keys.length; i++) {
            String key = keys[i];
            put(key, getDefaultVal(key));
        }                
    }

    public String getTooltip(String pKey) {
        return null;
    }         
    
    /** Form data may want to provide it's own document... just return null, if don't feel like it*/
    public Document getDocumentForTextField(String pKey) {
        return null;
    }
    
    /** Form data may want to provide it's own document... just return null, if don't feel like it*/
    public ComboBoxModel getDocumentForComboBox(String pKey) {
        return null;
    }     
    
    public static class ToMapWrapperConverter implements Converter<FormData, Map<String, Object>> {
        public Map convert(FormData p) {
            return ((FormData)p).asMap();    
        }
    }
    
    public static class FieldExtractor implements Converter<FormData, Object> {
        private String mFieldName;
        
        public FieldExtractor(String pFieldName) {
            mFieldName = pFieldName;
        }
        
        public Object convert(FormData p) {
            return p.get(mFieldName);    
        }
    }
    
    public String getDependentField(String pFieldToDependOn) {
        return null;
        // by default, we do not know any dependent fields
    }
    
    public void updateDependentField(String pFieldName) {
        throw new RuntimeException("Cannot update dependent fields!");
        // by default, we do not know of any dependent fields   
    }
    
    
    

}
