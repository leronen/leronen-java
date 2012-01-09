package util.converter;

import org.json.*;

/** extracts a "field" of intrest from object, in this case the field is the element at a certain index of a List */
public class JSONObjectStringExtractor implements Converter<JSONObject, String> {

    private String mKey;
    
    public JSONObjectStringExtractor(String pKey) {
        mKey = pKey;
    }
    
    public String convert(JSONObject json) {        
        return json.optString(mKey);                
    }
    
}
