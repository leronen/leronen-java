package util.collections;

/**
 * Recall that unfortunately interfaces Converter and Function are
 * not compatible.
 *
 * 
 */
public interface Function<P,V> {
    public String getName();
    public V compute(P p);
}
