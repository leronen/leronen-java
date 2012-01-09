package util.converter;

/** A "dummy" converter */
public class IdentityConverter implements Converter {   
    public Object convert(Object pParam) {
        return pParam;
    }
}

