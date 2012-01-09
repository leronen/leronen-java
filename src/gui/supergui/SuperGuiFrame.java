package gui.supergui;
      
    
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** The default stand-alone supergui application */
public class SuperGuiFrame extends JFrame {
        
    private String TITLE = "Super GUI";
    private Dimension SIZE = new Dimension(1000, 600);
        
    private SuperGui mSuperGui;
           
    public SuperGuiFrame() {
        init();
    }

    private void init() {
        mSuperGui = new SuperGui();
        mSuperGui.init();
                                                                    
        this.getContentPane().setLayout(new BorderLayout());        
        this.getContentPane().add(mSuperGui, BorderLayout.CENTER);                
        this.setTitle(TITLE);
        this.setSize(SIZE);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {                
                close();
            }
        });                
    }
        
    private void close() {
        System.exit(0);    
    }
                                        
    public static void main (String[] args) {
        SuperGuiFrame frame = new SuperGuiFrame();
        // frame.show();        
        frame.setVisible(true);
    }
          

}
