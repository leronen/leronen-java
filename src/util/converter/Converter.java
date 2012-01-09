package util.converter;

/** An arbitrary-valued function */
public interface Converter <T1, T2> {   
    public T2 convert(T1 pParam);
}

