package util.event;

/** The most general model listener interface imaginable */
public interface ModelListener {
    
    public void dataChanged(Object pSource);
    
    public void itemRenamed(Class itemClass, Object pOldId, Object pNewId);

}






