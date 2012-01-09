package util.dbg;

import gui.view.*;

import java.io.*;

public final class LogViewer {
    
    public static void main (String[] args) {
        try {        
            /*
            String logFile = args[0];
            Log log = new Log(logFile);
            JTree asJTree = log.asJTree();
            DefaultMutableTreeNode firstErrorNode = log.getFirstErrorNode();
            if (firstErrorNode != null) {
                Logger.info("Scrolling error node to visible: "+firstErrorNode);
                asJTree.scrollPathToVisible(new TreePath(firstErrorNode.getPath()));
            }                                     
            DummyApplication.showComponent(asJTree, "Log Viewer: "+logFile, true);
            */
            File logFile = new File(args[0]);                                 
            LogView logView = new LogView(logFile);
            ViewManager.getDefaultInstance().showView(logView, true);
        }
        catch (IOException e) {            
            e.printStackTrace();
        }
    }
}
