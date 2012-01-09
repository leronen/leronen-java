package gui.view;

import gui.menu.*;

import javax.swing.*;

/** A default implementation of View, for showing arbitraty components through a view manager */
public class DefaultView extends AbstractView {

    private String mName;
    private JComponent mComponent;    

    public void annihilate() {
        mComponent = null;
    }
    
    public DefaultView(String pName, JComponent pComponent) {
        mName = pName;
        mComponent = pComponent;        
    }

    protected DefaultView(String pName) {
        mName = pName;                
    }                        
    
    public void setComponent(JComponent pComponent) {
        mComponent = pComponent;
    }
        
    public String getMenubarId() {
        return MenuManager_old.MENUBAR_ID_VIEW;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setName(String pName) {
        mName = pName;
    }
        
    public JComponent getComponent() {
        return mComponent;    
    }
                    
               
    
}
