package gui.supergui;

import gui.form.*;
import gui.table.*;

import util.*;
import util.process.*;
import util.dbg.*;
import util.collections.*;
import util.io.*;
import util.converter.*;

import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;
import java.io.*;

public class SuperGui extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7981982170671338225L;
	public static final String FIELD_ID_DIR = "DIR";
    public static final String FIELD_ID_FILE_NAME_FILTER = "FILE_NAME_FILTER";
    public static final String FIELD_ID_EXPR_TO_REQUIRE = "EXPR_TO_REQUIRE";
    public static final String FIELD_ID_EXPR_TO_EXCUDE = "EXPR_TO_EXCLUDE";
    public static final String FIELD_ID_LINE_TO_APPEND = "LINE_TO_APPEND"; 
    public static final String FIELD_ID_LINE_TO_INSERT = "LINE_TO_INSERT"; 
    public static final String FIELD_ID_LINE_TO_INSERT_AFTER = "LINE_TO_INSERT_AFTER";
    public static final String FIELD_ID_REGEX_TO_REPLACE = "REGEX_TO_REPLACE";
    public static final String FIELD_ID_REPLACEMENT_TEXT = "REPLACEMENT_TEXT";
    public static final String FIELD_ID_CMD_LINE = "CMD_LINE";

    public static final String PROPERTY_ID_LAYOUT_TYPE = "LAYOUT_TYPE";
    public static final String LAYOUT_TYPE_VERTICAL = "vertical";
    public static final String LAYOUT_TYPE_HORIZONTAL = "horizontal";
    public static final String DEFAULT_LAYOUT_TYPE = LAYOUT_TYPE_HORIZONTAL;
    public static final String[] LAYOUT_TYPE_OPTIONS = { LAYOUT_TYPE_VERTICAL, LAYOUT_TYPE_HORIZONTAL };       
       
    private JSplitPane mRootSplitPane;
    private JFileChooser mFileChooser;
    
    private FilterParams mFilterParams;
    private InsertLineOperationParams mInsertLineOperationParams;
    private AppendLineOperationParams mAppendLineOperationParams;
    private ReplaceOperationParams mReplaceOperationParams;
    private CmdLineOperationParams mCmdLineOperationParams;
    
    private FormPanel mFilterParamsFormPanel;
    private FormPanel mInsertLineOperationParamsFormPanel;
    private FormPanel mAppendLineOperationParamsFormPanel;
    private FormPanel mReplaceOperationParamsFormPanel;
    private FormPanel mCmdLineOperationParamsFormPanel;    
    
    private JTabbedPane mOperationTabbedPane;
    private Box mRightmostBox;
       
    private JTable mTargetFileTable;          

    private DetailedFileList mTargetFiles;
    private JScrollPane mTargetFileListScrollPane;       
       
    private JPanel mButtonPanel;           
       
    private Map mProps;       
    
    public SuperGui() {
        this(new HashMap());
    }       
    
    public void setProps(Map pProps) {
        mProps = pProps;    
    }
    
    public SuperGui(Map pProps) {
        mProps = pProps;    
    }        
    
    /** Actual initialization, as constructor does next to nothing */
    public void init() {        
        setLayout(new BorderLayout());
        
        mTargetFiles = new DetailedFileList();
        
        mFileChooser = new JFileChooser();                    
            
        mFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);                    
        mFileChooser.addPropertyChangeListener(
            JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    dbgMsg("PropertyChange: "+e.getPropertyName()+": "+e.getNewValue());
                    mFilterParams.put(FIELD_ID_DIR, mFileChooser.getSelectedFile().getPath());
                    mFilterParamsFormPanel.modelToGUI();
                    // listTargetFiles();
                }
            }
        );
    
        mFileChooser.setControlButtonsAreShown(false);
        mFilterParams = new FilterParams ();
        mInsertLineOperationParams = new InsertLineOperationParams ();
        mAppendLineOperationParams = new AppendLineOperationParams ();
        mReplaceOperationParams = new ReplaceOperationParams ();
        mCmdLineOperationParams = new CmdLineOperationParams ();            
        mFilterParamsFormPanel = new FormPanel(mFilterParams);
        mOperationTabbedPane = new JTabbedPane();
        mInsertLineOperationParamsFormPanel = new FormPanel(mInsertLineOperationParams);
        mAppendLineOperationParamsFormPanel = new FormPanel(mAppendLineOperationParams);
        mReplaceOperationParamsFormPanel = new FormPanel(mReplaceOperationParams);
        mCmdLineOperationParamsFormPanel = new FormPanel(mCmdLineOperationParams);
        mOperationTabbedPane.addTab("insert lines", mInsertLineOperationParamsFormPanel);
        mOperationTabbedPane.addTab("append line", mAppendLineOperationParamsFormPanel);
        mOperationTabbedPane.addTab("replace", mReplaceOperationParamsFormPanel);
        mOperationTabbedPane.addTab("cmd line", mCmdLineOperationParamsFormPanel);        
        
        int layoutType = getLayoutType().equals(LAYOUT_TYPE_HORIZONTAL) ? 
                         JSplitPane.HORIZONTAL_SPLIT :
                         JSplitPane.VERTICAL_SPLIT;
                
        mRootSplitPane = new JSplitPane(layoutType);
        mRootSplitPane.setLeftComponent(mFileChooser);                                               
        
        mButtonPanel = new JPanel(new FlowLayout());
        JButton listFilesButton = new JButton("list files");
        listFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mFilterParamsFormPanel.guiToModel();
                listTargetFiles();
            }
        });            
        
        JButton doOperationButton = new JButton("do operation");
        doOperationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (mOperationTabbedPane.getSelectedComponent()==mInsertLineOperationParamsFormPanel) {
                    mInsertLineOperationParamsFormPanel.guiToModel();
                    performInsertLinesOperation(mInsertLineOperationParams.getLineToInsert(),
                                                mInsertLineOperationParams.getLineToInsertAfter());
                }
                else if (mOperationTabbedPane.getSelectedComponent()==mAppendLineOperationParamsFormPanel) {
                    mAppendLineOperationParamsFormPanel.guiToModel();
                    performAppendLinesOperation(mAppendLineOperationParams.getLineToAppend());
                }
                else if (mOperationTabbedPane.getSelectedComponent()==mReplaceOperationParamsFormPanel) {
                    mReplaceOperationParamsFormPanel.guiToModel();
                    performReplaceOperation(mReplaceOperationParams.getRegexToReplace(),
                                            mReplaceOperationParams.getReplacementText());                                            
                }
                else if (mOperationTabbedPane.getSelectedComponent()==mCmdLineOperationParamsFormPanel) {
                    mCmdLineOperationParamsFormPanel.guiToModel();
                    performCommandLineOperation(mCmdLineOperationParams.getCmdLine());                                            
                }
                else {
                    throw new RuntimeException("Illegal selected tab: "+mOperationTabbedPane.getSelectedComponent());
                }
            }
        });            
        
        mButtonPanel.add(listFilesButton);        
        mButtonPanel.add(doOperationButton);
                                        
        mTargetFileTable = new JTable(mTargetFiles);
        mTargetFileTable.addMouseListener(new FileListMouseListener()); 
        
        mTargetFileListScrollPane = new JScrollPane(mTargetFileTable);
                
        mRightmostBox = Box.createVerticalBox();
        
        Box controlsBox = Box.createVerticalBox();
        
        controlsBox.add(mFilterParamsFormPanel);
        controlsBox.add(mButtonPanel);                
        controlsBox.add(mOperationTabbedPane);

        JSplitPane rightMostLowerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        rightMostLowerSplitPane.setTopComponent(controlsBox);        
        rightMostLowerSplitPane.setBottomComponent(mTargetFileListScrollPane);
        
        mRightmostBox.add(rightMostLowerSplitPane);            
        
        mRootSplitPane.setRightComponent(mRightmostBox);
                
        add(mRootSplitPane, BorderLayout.CENTER);                                        
    }
    
    private String getLayoutType() {
        if (mProps == null) {
            return DEFAULT_LAYOUT_TYPE;
        }
        else {
            String candidate = (String)mProps.get(PROPERTY_ID_LAYOUT_TYPE);
            if (candidate == null) {                
                return DEFAULT_LAYOUT_TYPE;
            }
            else {
                return candidate;        
            }
        }            
    }
    
    /** Override this if needed */    
    protected void doOpenFile(File pFile) {
        try {
            String editor = "ue";
            ProcessUtils.openWithEditor(pFile, editor, null);
        }
        catch (Exception e) {
            reportError("Failed opening file: "+pFile+"; "+e.toString());
        }
    }

    /** Override this if needed */
    protected void reportError(String pError) {
        Logger.error(pError);            
    }
    
    /** Override this */
    protected void doSetStatusText(String pStatusText) {
        dbgMsg(pStatusText);
    }                        
    
    private void performInsertLinesOperation(String pLineToInsert, String pLineToInsertAfter) {
        try {
            File[] fileList = mTargetFiles.getFiles();
            
            // find occurences of the line pLineToInsertAfter in each of the files...
            LineCondition lineToInsertAfterCondition = new ContainsPatternLineCondition("^.*"+pLineToInsertAfter+".*$");
            MultiMap linesByFile = FileUtils.findLinesMatchingCondition(fileList, lineToInsertAfterCondition);
            dbgMsg("*****************************************************");
            dbgMsg("lines to insert before addition:\n"+linesByFile);
            
            // silentyly prune the lines, so that only the first one remains...
            linesByFile.prune();
            
            // add 1 to line numbers to insert after the corresponding line!
            linesByFile = ConversionUtils.convert(linesByFile, new IntAdderConverter(1));
            dbgMsg("*****************************************************");
            dbgMsg("lines to insert after addition:\n"+linesByFile);
                    
            dbgMsg("Inserting line: \""+pLineToInsert+"\" into "+fileList.length+" files.");
            FileUtils.insertLineIntoFiles(linesByFile, pLineToInsert);
            doSetStatusText(""+linesByFile.keySet().size()+" files modified.");
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performAppendLinesOperation(String pLineToAppend) {
        try {
            File[] fileList = mTargetFiles.getFiles();                                                        
            dbgMsg("Appending line: \""+pLineToAppend+"\" into "+fileList.length+" files.");
            FileUtils.appendLineIntoFiles(fileList, pLineToAppend);
            doSetStatusText(""+fileList.length+" files modified.");
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void performReplaceOperation(String pRegexToReplace, String pReplacementText) {
        try {
            File[] fileList = mTargetFiles.getFiles();            
            ReplaceFileOperation oper = FileUtils.replaceInFiles(pRegexToReplace, pReplacementText, fileList);
                                    
            doSetStatusText(oper.getLineCount()+" lines in "+oper.getFileCount()+" files modified.");                        
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void performCommandLineOperation(String pCommandLine) {
        try {
            File[] fileList = mTargetFiles.getFiles();
            for (int i=0; i<fileList.length; i++) {            
                String cmd = pCommandLine+" "+fileList[i];
                String msg = "Executing command line: "+cmd; 
                dbgMsg(msg);
                doSetStatusText(msg);                    
                ProcessUtils.executeCommand(cmd, 
                                                 null, // dir
                                                 null); // process owner
            }
            doSetStatusText(""+fileList.length+" commands executed.");                                                                        
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
    }
                
        
    private void listTargetFiles() {
        String dir = mFilterParams.getDir();
        String fileNameFilter = mFilterParams.getFileNameFilter();
        if (fileNameFilter != null && fileNameFilter.equals("")) {
            fileNameFilter = null;
        }
        String exprToRequire = mFilterParams.getExpr();
        String exprToExclude = mFilterParams.getExprNotToMatch();        
        
        dbgMsg("starting listTargetFiles...");
        try {            
            File[] files = FileUtils.filesInDir(new File(dir), true, fileNameFilter);
            Arrays.sort(files);
            int numFiles = files.length;
            dbgMsg("found "+numFiles+" files");                                                            
            if (exprToRequire != null &&  !(exprToRequire.equals(mFilterParams.getDefaultVal(FIELD_ID_EXPR_TO_REQUIRE)))) {
                // get only files containing pExprToMatch
                ContainsPatternFileCondition containsExprCondition = new ContainsPatternFileCondition("^.*"+exprToRequire+".*$");
                files = FileUtils.findFilesMatchingCondition(files, containsExprCondition);
                dbgMsg("pruned away "+(numFiles-files.length)+" files files because they did not contain expr to require");
                numFiles = files.length;
            }                        
            if (exprToExclude != null && !(exprToExclude.equals(mFilterParams.getDefaultVal(FIELD_ID_EXPR_TO_EXCUDE)))) {                
                // get only files not containing pExprNotToMatch
                ContainsPatternFileCondition containsLineCondition = new ContainsPatternFileCondition("^.*"+exprToExclude+".*$");
                FileCondition doesNotContainLineCondition = new NotFileCondition(containsLineCondition);
                files = FileUtils.findFilesMatchingCondition(files, doesNotContainLineCondition);
                dbgMsg("pruned away "+(numFiles-files.length)+" files because they contained expr to exclude");
                numFiles = files.length;                                            
            }
            dbgMsg("setting data: "+numFiles+" files matched as target files");                                               
            mTargetFiles.setData(files);
            doSetStatusText("found "+numFiles+" matching files.");
        }        
        catch (Exception e) {
            e.printStackTrace();
        }
    }

                            
    public static void main (String[] args) {
        SuperGuiFrame frame = new SuperGuiFrame();
        // frame.show();        
        frame.setVisible(true);
    }

    private class FilterParams extends LinkedHashMapFormData {
        private String[] FIELDS = { FIELD_ID_DIR, FIELD_ID_FILE_NAME_FILTER, FIELD_ID_EXPR_TO_REQUIRE, FIELD_ID_EXPR_TO_EXCUDE };                                
        
        public String getFormName() {
            return "file filtering params";
        }                            
        public String[] getAllKeys() {
            return FIELDS;
        }
        // public Set getObligatoryFields() {
            // return new HashSet(Arrays.asList(FIELDS));    
        // }        
        
        public String getDir() {                
            return (String)get(FIELD_ID_DIR);
        }
        
        public String getFileNameFilter() {
            return (String)get(FIELD_ID_FILE_NAME_FILTER);    
        }
        
        public String getExpr() {
            return (String)get(FIELD_ID_EXPR_TO_REQUIRE);
        }
        
        public String getExprNotToMatch() {            
            return (String)get(FIELD_ID_EXPR_TO_EXCUDE);
        }                        
        public Object getDefaultVal(String pKey) {
            return "";
        }                
        public void writeToStream(PrintWriter pWriter) {
            throw new UnsupportedOperationException("Not possible");
        }                                                                              
    }
    
    private class InsertLineOperationParams extends LinkedHashMapFormData {
        private String[] FIELDS = { FIELD_ID_LINE_TO_INSERT, FIELD_ID_LINE_TO_INSERT_AFTER };                                
        
        public String getFormName() {
            return "insert line operation params";
        }                            
        public String[] getAllKeys() {
            return FIELDS;
        }
        // public Set getObligatoryFields() {
            // return new HashSet(Arrays.asList(FIELDS));    
        // }                                        
        public String getLineToInsert() {
            return(String)get(FIELD_ID_LINE_TO_INSERT);
        }
        public String getLineToInsertAfter() {
            return(String)get(FIELD_ID_LINE_TO_INSERT_AFTER);
        }                                                                            
        
        /** implementation can freely throw UnsupportedOperationException in the case that it is an read-only-form */
        public void writeToStream(PrintWriter pWriter) {
            throw new UnsupportedOperationException("Not possible");
        }                                                                              
    }
    
    private class AppendLineOperationParams extends LinkedHashMapFormData {
        private String[] FIELDS = { FIELD_ID_LINE_TO_APPEND };                                
        
        public String getFormName() {
            return "append line operation params";
        }                            
        public String[] getAllKeys() {
            return FIELDS;
        }
        // public Set getObligatoryFields() {
            // return new HashSet(Arrays.asList(FIELDS));    
        // }                                        
        public String getLineToAppend() {
            return(String)get(FIELD_ID_LINE_TO_APPEND);
        }                                                                                         
        /** implementation can freely throw UnsupportedOperationException in the case that it is an read-only-form */
        public void writeToStream(PrintWriter pWriter) {
            throw new UnsupportedOperationException("Not possible");
        }                                                                              
    }
                
    
    private class CmdLineOperationParams extends LinkedHashMapFormData {
        private String[] FIELDS = { FIELD_ID_CMD_LINE };                                
        
        public String getFormName() {
            return "cmd line operation params";
        }                            
        public String[] getAllKeys() {
            return FIELDS;
        }                                
                
        public String getCmdLine() {
            return(String)get(FIELD_ID_CMD_LINE);
        }                              
        
        /** implementation can freely throw UnsupportedOperationException in the case that it is an read-only-form */
        public void writeToStream(PrintWriter pWriter) {
            throw new UnsupportedOperationException("Not possible");
        }                                                                              
    }
        
    private class ReplaceOperationParams extends LinkedHashMapFormData {
        private String[] FIELDS = { FIELD_ID_REGEX_TO_REPLACE, FIELD_ID_REPLACEMENT_TEXT };                                
        
        public String getFormName() {
            return "replace operation params";
        }                            
        public String[] getAllKeys() {
            return FIELDS;
        }                                
                
        public String getRegexToReplace() {
            return(String)get(FIELD_ID_REGEX_TO_REPLACE);
        }

        public String getReplacementText() {
            return(String)get(FIELD_ID_REPLACEMENT_TEXT);
        }                
                                
        public boolean excludeFromSerialization(String pKey) {
            return false;
        }                    
        
        /** implementation can freely throw UnsupportedOperationException in the case that it is an read-only-form */
        public void writeToStream(PrintWriter pWriter) {
            throw new UnsupportedOperationException("Not possible");
        }                                                                              
    }
                                
    private class FileListMouseListener extends MouseAdapter {
 
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int selectedRow = mTargetFileTable.getSelectionModel().getMinSelectionIndex();                
                File selectedFile = mTargetFiles.getFile(selectedRow);
                dbgMsg("double clicked on: "+selectedFile);
                doOpenFile(selectedFile);                
            }
        }
   }
                
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("SuperGui: "+pMsg);
    }
    
}
