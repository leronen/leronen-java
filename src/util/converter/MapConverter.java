package util.converter;

import java.util.Map;

import util.dbg.Logger;

/**
 * Converts objects of type K to objects of type T, as defined by a mapping.
 * Only references are copied, so resulting objects should not be modified.
 */
public final class MapConverter<K,V> implements Converter<K,V> {

    public static enum NotFoundBehauvior {
        ERROR,
        RETURN_ORIGINAL,
        RETURN_NULL,
        RETURN_DEFAULT,
        RETURN_DEFAULT_AND_WARN,
        RETURN_ORIGINAL_AND_WARN,
        RETURN_TO_STRING,
        RETURN_TO_STRING_AND_WARN;
    }

    private final NotFoundBehauvior mNotFoundBehauviour;

    private Map<K,V> mMap;

    private V mDefaultVal; // only used with NOT_FOUND_BEHAUVIOR_RETURN_DEFAULT

    public MapConverter(Map<K,V> pMap, NotFoundBehauvior pNotFoundBehauviour) {
        if (pNotFoundBehauviour == NotFoundBehauvior.RETURN_DEFAULT || pNotFoundBehauviour == NotFoundBehauvior.RETURN_DEFAULT_AND_WARN) {
            throw new RuntimeException("Call constructor with default val parameter (notfoundbehaviour: " + pNotFoundBehauviour + ")");
        }
        mNotFoundBehauviour = pNotFoundBehauviour;
        mMap = pMap;
    }

    public void setMap(Map pData) {
        mMap = pData;
    }

    public MapConverter(Map<K,V> pMap, NotFoundBehauvior pNotFoundBehauviour, V pDefaultVal) {
        if (pNotFoundBehauviour != NotFoundBehauvior.RETURN_DEFAULT && pNotFoundBehauviour != NotFoundBehauvior.RETURN_DEFAULT_AND_WARN) {
            throw new RuntimeException("Can only specify NotFoundBehauvior.RETURN_DEFAULT or NotFoundBehauvior.RETURN_DEFAULT_AND_WARN in this constructor");
        }
        mNotFoundBehauviour = pNotFoundBehauviour;
        mMap = pMap;
        mDefaultVal = pDefaultVal;
    }

    public MapConverter(Map<K,V> pMap, V pDefaultVal) {
        mNotFoundBehauviour = NotFoundBehauvior.RETURN_DEFAULT;
        mMap = pMap;
        mDefaultVal = pDefaultVal;
    }

    @Override
    public V convert(K pObj) {
        V val = mMap.get(pObj);

        if (val == null) {
            if (mNotFoundBehauviour == NotFoundBehauvior.ERROR) {
                throw new RuntimeException("Cannot convert: object not found in map: "+pObj);
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_ORIGINAL) {
                val = (V)pObj;
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_ORIGINAL_AND_WARN) {
            	Logger.warning("Object not found in map: "+pObj+"; returning original val...");
            	val = (V)pObj;
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_TO_STRING_AND_WARN) {
                Logger.warning("Object not found in map: "+pObj+"; returning value returned by toString()...");
                val = (V)pObj.toString();
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_TO_STRING) {
                val = (V)pObj.toString();
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_NULL) {
                // OK, lets return that null
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_DEFAULT) {
                val = mDefaultVal;
            }
            else if (mNotFoundBehauviour == NotFoundBehauvior.RETURN_DEFAULT_AND_WARN) {
            	Logger.warning("Object not found in map: "+pObj+"; returning default val: "+mDefaultVal);
            	val = mDefaultVal;
            }
            else {
                throw new RuntimeException("Illegal not found behauviour code: "+mNotFoundBehauviour);
            }
        }
        return val;
    }
}
