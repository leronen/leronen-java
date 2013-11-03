package leronen.projectsystem;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.collections.MultiMap;

public class Project {
	
	static final String COLUMN_NAME_ID       = "ID";
	static final String COLUMN_NAME_TITLE    = "TITLE";
	static final String COLUMN_NAME_ROOT_DIR = "ROOT_DIR";
	static final String COLUMN_NAME_SETENV_SCRIPT = "SETENV_SCRIPT";
	
	int id;
	String title;
	String rootDir;
	String setenvScript;
	
	MultiMap<String,String> properties;
	
	Project(int id, String title, String rootDir, String setenvScript, MultiMap<String, String> properties) {
		this.id = id;
		this.title = title;
		this.rootDir = rootDir;
		this.setenvScript = setenvScript;
		if (properties != null) {
			this.properties = properties;
		}
		else {
			this.properties = new MultiMap<String,String>();
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(COLUMN_NAME_ID, id);
		json.put(COLUMN_NAME_TITLE, title);
		json.put(COLUMN_NAME_ROOT_DIR, rootDir);
		if (setenvScript != null) {
			json.put(COLUMN_NAME_SETENV_SCRIPT, setenvScript);
		}
		
		for (String key: properties.keySet()) {
			Set<String> values = properties.get(key);
			if (values.size() ==  0) {
				// wtf, no action.
			}
			else if (values.size() == 1) {
				// just one
				json.put(key, values.iterator().next());
			}
			else {
				// mäny
				JSONArray propsJSON = new JSONArray();
				for (String val: values) {
					propsJSON.put(val);
				}
				json.put(key,  propsJSON);
			}			
		}
		return json;
	}
	
	public String toString() {
		try {
			return toJSON().toString();
		}
		catch (JSONException e) {
			throw new RuntimeException("Failed formatting project "+this.id+"as json");		
		}
	}
			
}
