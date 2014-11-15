package gui.view;

import gui.action.*;
import gui.application.*;
import gui.menu.*;

import java.awt.*;

import javax.swing.*;


public class ViewContainerFrame extends JFrame implements ViewContainer {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3242755630000904316L;
	private IView mView;
    private ViewManager mViewManager;
    private boolean mShowInScrollPane;
    private JScrollPane mScrollPane;
    
    private JComponent mCurrentComponent;
        
    public ViewContainerFrame(IView pView, boolean pShowInScrollPane, ViewManager pViewManager) {               
        super(pView.getName());        
        mViewManager = pViewManager;
        mView = pView;
        pView.setViewContainer(this);
        mShowInScrollPane = pShowInScrollPane;
        
        initGui();                                             
    }

    public void viewComponentChanged() {
        JComponent prevComponent = mCurrentComponent;
        mCurrentComponent = mView.getComponent();          
        if (mShowInScrollPane) {            
            mScrollPane.setViewportView(mCurrentComponent);
        }
        else {
            getContentPane().remove(prevComponent);            
            getContentPane().add(mCurrentComponent, BorderLayout.CENTER);
            mCurrentComponent.revalidate();
        }        
    }
    
    private void initGui() {
        IMain main =  mViewManager.getMain();
        if (main != null) {
            makeMenu();
        }
                        
        getContentPane().setLayout(new BorderLayout());
        mCurrentComponent = mView.getComponent();
        if (mShowInScrollPane) {            
            mScrollPane = new JScrollPane(mCurrentComponent);
            getContentPane().add(mScrollPane, BorderLayout.CENTER);
        }
        else {            
            getContentPane().add(mCurrentComponent, BorderLayout.CENTER);
        }
        
        
//        addWindowFocusListener(new OurWindowFocusListener());
//        addWindowStateListener(new OurWindowStateListener());
        
        Dimension preferredSize = mView.getPreferredSize();
        if (preferredSize != null) {
            setSize(preferredSize);
        }
        else {
            pack();
        }
    }   

    private void makeMenu() {
        IMain main =  mViewManager.getMain();        
        MenuManager_old menuManager = main.getMenuManager_old();
        String menuBarId = mView.getMenubarId();
        if (menuBarId != null) {
            CommandHandler fallbackHandler = mView.getCommandHandler();
            if (fallbackHandler == null) {
                fallbackHandler = mViewManager.getMain().getDefaultCommandHandler();
            }               
            // OK, we have a command handler
            JMenuBar menuBar = menuManager.makeMenuBar(menuBarId, new ViewMenuCommandHandler(fallbackHandler));
            setJMenuBar(menuBar);
        }
    }                  
    
    public IView getView() {
        return mView;
    }
    
    public void close() {
        dispose();        
    }
        
    /** This makes the frame visible in the event dispatch thread.... */                    
    public void makeVisible() {
        Runnable runner = new FrameShower();
        EventQueue.invokeLater(runner);                        
    }     
                 
    private class FrameShower implements Runnable {
         
         public void run() {
             setVisible(true);
             toFront();             
             
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             Dimension frameSize = getSize();
             
             int extraX = (int)(screenSize.getWidth() - frameSize.getWidth());
             int extraY = (int)(screenSize.getHeight() - frameSize.getHeight());
             
             setLocation(extraX/2, extraY/2);
             
             if (mView.prefersToFillTheWholeScreen()) {
                // dbgMsg("************************");
                // dbgMsg("Maximizing view: "+pView);
                // dbgMsg("************************");                
                // Toolkit toolkit = Toolkit.getDefaultToolkit();                
                // Dimension dimension = toolkit.getScreenSize();                                
                // frame.setSize(dimension);                                                 
                int state = getExtendedState();
                
                // Set the maximized bits
                state |= Frame.MAXIMIZED_BOTH;
            
                // Maximize the frame
                setExtendedState(state);
                mCurrentComponent.revalidate();                                
            }
            else {
                // dbgMsg("************************");
                // dbgMsg("packing view: "+pView);
                // dbgMsg("************************");
                // frame.pack();
            }                                                
         }             
     }

    ////////////////////////////////////////////////////////////
    // implementation for interface CommandHandler
    ////////////////////////////////////////////////////////////
    private class ViewMenuCommandHandler extends AbstractMenuAwareCommandHandler {
        private ViewMenuCommandHandler(CommandHandler pFallbackHandler) {
            super(pFallbackHandler);
        }    
            
        public boolean handleCommand(String pCommand) {
            if (pCommand.equals(MenuManager_old.CMD_ID_CLOSE_ALL)) {
                mViewManager.closeAll();
                return true;
            }
            else if (pCommand.equals(MenuManager_old.CMD_ID_CLOSE)) {
                mViewManager.closeView(mView.getName());
                return true;   
            }
            else if (pCommand.equals(MenuManager_old.CMD_ID_REFRESH)) {
                mView.refresh();
                return true;
            }
            else {
                // not handled 
                return false;
            }   
        }
    }
    
//    private class OurWindowStateListener implements WindowStateListener {
//
//        @Override
//        public void windowStateChanged(WindowEvent arg0) {
//            // TODO Auto-generated method stub
//            Logger.info("Window state changed in "+ViewContainerFrame.this+
//                        ":"+arg0);
//        }
//        
//    }
//    
//    private class OurWindowFocusListener implements WindowFocusListener {
//
//        @Override
//        public void windowGainedFocus(WindowEvent arg0) {
//            Logger.info("Window gained focus: "+ViewContainerFrame.this);                                
//        }
//
//        @Override
//        public void windowLostFocus(WindowEvent arg0) {
//            Logger.info("Window lost focus: "+ViewContainerFrame.this);                                            
//        }
//
//                
//        
//    }
    
    
            
}
