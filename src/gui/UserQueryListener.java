package gui;

import gui.form.*;

public interface UserQueryListener {

    public void onCancel();
    
    public void onOk(FormData pForm);
    
    public void onOk(Object pValue);
    
        
}
