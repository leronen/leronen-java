package gui.view;

import util.*;
import util.dbg.*;
import javax.swing.*;
import javax.swing.tree.*;

import java.io.*;

public class LogView extends DefaultView {
    
    // data             
    private Log mLog;    
    private JTree mLogTree;
             
    public LogView(File pLogFile) throws IOException {
        super(formatTitle(pLogFile));                                
        mLog = new Log(pLogFile);
        initGui();
    }
                                                     
    public LogView(Log pLog) {
        super(formatTitle(pLog.getFile()));                                
        mLog = pLog;
        initGui();
    }       
              
    public static String formatTitle(File pLogFile) {
        String modified = DateUtils.formatDate(pLogFile.lastModified());
        return "log viewer: "+pLogFile+" ("+modified+")";
    }              
              
    public void initGui() {                 
        mLogTree = mLog.asJTree();                                                                                       
                        
        setComponent(mLogTree);                                        
    }        

    public void afterShownInitialization() {
        DefaultMutableTreeNode firstErrorNode = mLog.getFirstErrorNode();
        if (firstErrorNode != null) {
            Logger.info("Scrolling error node to visible: "+firstErrorNode);
            mLogTree.scrollPathToVisible(new TreePath(firstErrorNode.getPath()));                    
        }                        
    }                                                                                                                                                                        
                       
}
