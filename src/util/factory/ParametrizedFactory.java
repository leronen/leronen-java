package util.factory;

/** 
 * Let's decide that a parametrized factory can also make objects without param.
 * This is to maintain some backward-compatability...
 */
public interface ParametrizedFactory<T,P> extends Factory<T> {

	T makeObject();
    T makeObject(P pParam);
    // T makeObject(Object pParam);

}
