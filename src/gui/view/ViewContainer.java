package gui.view;

public interface ViewContainer {

    public IView getView();
    
    public void makeVisible();
    
    public void close();
    
    public void viewComponentChanged();
}
