package gui.application;

import gui.*;

import javax.swing.*;

/** Functionality has moved to greener bit-fields */
public class DummyApplication {
    public static void showComponent(JComponent pComponent) {
        GuiUtils.showComponent(pComponent);
        // showComponent(pComponent, "Dummy application");                        
    }

    public static void showComponent(JComponent pComponent, String pTitle) {
        GuiUtils.showComponent(pComponent, pTitle);
        // showComponent(pComponent, pTitle, false);
    }
 
    public static void showComponent(JComponent pComponent, String pTitle, boolean pShowInScrollPane) {
        GuiUtils.showComponent(pComponent, pTitle, pShowInScrollPane);
        /*
        JFrame frame = new JFrame(pTitle);        
        frame.getContentPane().setLayout(new BorderLayout());
        if (pShowInScrollPane) {
            JScrollPane scrollPane = new JScrollPane(pComponent);
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        }
        else {
            frame.getContentPane().add(pComponent, BorderLayout.CENTER);
        }
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
*/        
    }
 
       
}
