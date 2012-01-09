package gui.view;

import util.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

public class SimpleTextView extends DefaultView {
    
    private static final Dimension PREFERRED_SIZE = new Dimension(800,600);
                
    // data             
    private String[] mLines;
    
    private JTextArea mTextArea;
    
    public SimpleTextView(File pFile) throws IOException {
        super(pFile.getAbsolutePath());                              
        mLines = IOUtils.readLineArray(pFile);            
        
        initGui();
    }
    
    public SimpleTextView(String pTitle,
                          File pFile) throws IOException {
        super(pTitle);                              
        mLines = IOUtils.readLineArray(pFile);            
        
        initGui();
    }
                          
                    
    public SimpleTextView(String pTitle, 
                          String[] pText) {
        super(pTitle);                                
        mLines = pText;                                        
        
        initGui();
    }                                                                
                               
    public void initGui() {                 
        String concatenatedLines = StringUtils.arrayToString(mLines);
        mTextArea = new JTextArea(concatenatedLines);
        mTextArea.setLineWrap(true);
        mTextArea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        setComponent(mTextArea);                                        
    }     
    
    public Dimension getPreferredSize() {
        return PREFERRED_SIZE;    
    }
                                                                                                                                                               
                       
}
