package util;

import java.util.*;


/** Note that currently only works for single-threaded execution */ 
public final class DataStructureCache {

    private HashMap mData = new HashMap();
    
    private static DataStructureCache mSingletonInstance;
    
    private static DataStructureCache getInstance() {
        if (mSingletonInstance == null) {
            mSingletonInstance = new DataStructureCache();
        }
        return mSingletonInstance;
    }
    
    public static Object get(Object pKey) {
        return getInstance().mData.get(pKey);    
    }

    public static Object put(Object pKey, Object pVal) {
        return getInstance().mData.put(pKey, pVal);    
    }                

}
