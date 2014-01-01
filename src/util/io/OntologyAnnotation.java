package util.io;

import java.util.Map;
import java.util.Set;

import util.CollectionUtils;

public class OntologyAnnotation {
	public static final String COLUMN_NAME_FORM_ID           = "FORM_ID";;
	public static final String COLUMN_NAME_FORM_COLUMN       = "FORM_COLUMN";
	public static final String COLUMN_NAME_ONTOLOGY_ID       = "ONTOLOGY_ID";
	public static final String COLUMN_NAME_TERM_ID           = "TERM_ID";
	public static final String COLUMN_NAME_RELATIONSHIP_TYPE = "RELATIONSHIP_TYPE";
	
	public static final Set<String> ALL_COLUMNS = CollectionUtils.makeLinkedHashSet(
			COLUMN_NAME_FORM_ID, COLUMN_NAME_FORM_COLUMN, COLUMN_NAME_ONTOLOGY_ID,
			COLUMN_NAME_TERM_ID, COLUMN_NAME_RELATIONSHIP_TYPE);
	
	private Map<String, String> data;
	
	public String toString() {
    	StringBuffer buf = new StringBuffer();
    	boolean first = true;    	
    	for (String key: ALL_COLUMNS) { 
    		String val = data.get(key);
    		if (first) {
    			first = false;
    			buf.append(key+"="+val);
    		}
    		else {    			
    			buf.append("\n\t"+key+"="+val);
    		}    		    		
    	}
    	return buf.toString();
    }
	
}
