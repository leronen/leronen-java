package util.collections;

import java.util.Map;

import util.StringUtils;


public class Properties {

    private Map<String, String> mData;
    
    public Properties(Map<String, String> pData) {
        mData = pData;
    }
    
    public String getString(String pKey) {
        return mData.get(pKey);
    }
    
    public Double getDouble(String pKey) {
        String tmp = mData.get(pKey);
        return tmp != null ? Double.parseDouble(tmp) : null;
    }
    
    public Integer getInteger(String pKey) {
        String tmp = mData.get(pKey);
        return tmp != null ? Integer.parseInt(tmp) : null;
    }
    
    public Long getLong(String pKey) {
        String tmp = mData.get(pKey);
        return tmp != null ? Long.parseLong(tmp) : null;
    }
    
    public String toString() {
        return StringUtils.format(mData, "=", "\n");
    }
}
