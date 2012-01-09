package util.process;

public abstract class RunnableWithObject implements Runnable {

	public Object o;
	
	public RunnableWithObject(Object p) {
		o = p;
	}

	
}
