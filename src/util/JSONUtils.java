package util;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
    
    /**
     * Trivial deep cloning implementation that performs a JSONObject => String => JSONObject
     * conversion to minimize implementation effort.
     */
    public static JSONObject clone(JSONObject obj) throws RuntimeException {
        try {
            return new JSONObject(obj.toString());
        }
        catch (JSONException e) {
            throw new RuntimeException("Should not be possible fail this trivial operation");
        }
    }
}
