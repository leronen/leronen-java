package gui.form;

import gui.*;

import util.*; 

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import java.io.*;
import java.util.*;

public class FormPanel extends JPanel {
    
    private static final int X_PAD = 4;
    private static final int Y_PAD = 4;
    
    private FormData mModel;
    
    private FormLayout mLayout;      
    
    private JLabel[] mLabels;
    private JComponent[] mFields;
    
    private Map mFieldsByKey;    
    
    private ArrayList mListeners;
    
    /** Todo: what to do? */
    private static final int DEFAULT_WIDTH = 500;
    
    private boolean mMovingDataFromModelToGUI;
    
    // private static Font FONT = new Font("Dialog", Font.PLAIN, 12);
    
    public FormPanel(FormData pData) {
        mMovingDataFromModelToGUI = true;        
        mModel = pData;
        mListeners = new ArrayList();
        
        String[] keys = mModel.getAllKeys();
        mLabels = new JLabel[keys.length];        
        mFields = new JComponent[keys.length];
        mFieldsByKey = new HashMap();
        mLayout = new FormLayout (this);
        setLayout(mLayout);        
        // setFont(FONT);
        // font = getFont();
        Font font = new Font("Dialog", Font.PLAIN, 10);
        FontMetrics fm = getFontMetrics(font);
        
        // Logger.info("font: "+font);
        // Logger.info("font metrics: "+fm);
        
        int maxTextWidth = 120;
        for (int i=0; i<keys.length; i++) {
            int textWidth = fm.stringWidth(keys[i]);
            if (maxTextWidth<textWidth) {
                maxTextWidth = textWidth;
            }
        }
        
        for (int i=0; i<keys.length; i++) {            
            mLabels[i]=new JLabel(keys[i]);
            // does not work:
//            mLabels[i].setFocusable(true);
//            mLabels[i].setEnabled(true);
//            mLabels[i].setRequestFocusEnabled(true);
            mFields[i]=makeField(keys[i]);
            mFieldsByKey.put(keys[i], mFields[i]);
            int preferredHeight = 16;
            if (mFields[i] instanceof FileComponent) {
                preferredHeight = 30;
            }                
            
            mLabels[i].setPreferredSize(new Dimension(maxTextWidth+20, preferredHeight));
            mFields[i].setPreferredSize(new Dimension(300, preferredHeight));
            
            add(mLabels[i]);
            add(mFields[i]);
            
            // horizontal layout 
            mLayout.constrain(mLabels[i], FormLayout.LEFT, FormLayout.ATTACH_FORM, null, X_PAD);
            mLayout.constrain(mFields[i], FormLayout.LEFT, FormLayout.ATTACH_COMPONENT, mLabels[i], X_PAD);
            // vertical layout             
            if (i==0) {
                mLayout.constrain(mLabels[i], FormLayout.TOP, FormLayout.ATTACH_FORM, null, Y_PAD);
                mLayout.constrain(mFields[i], FormLayout.TOP, FormLayout.ATTACH_FORM, null, Y_PAD);
            }
            else {
                mLayout.constrain(mLabels[i], FormLayout.TOP, FormLayout.ATTACH_COMPONENT, mLabels[i-1], Y_PAD);
                mLayout.constrain(mFields[i], FormLayout.TOP, FormLayout.ATTACH_COMPONENT, mFields[i-1], Y_PAD);
            }                                        
        }        
        mMovingDataFromModelToGUI = false;
        
        modelToGUI();
                
        setFont(font);        
        
        createBorderIfNeeded();                        
    }
    
    public FormData getModel() {
        return mModel;
    }        
    
    private JComponent makeField(String pKey) {
        String type = mModel.getType(pKey);
        boolean editable = mModel.isEditable(pKey);
        JComponent comp = null;
        if (type.equals(FormData.TYPE_ENUM) && editable) {            
            ComboBoxModel comboModel = mModel.getDocumentForComboBox(pKey);
            if (comboModel == null) {
                Object[] possibleValues = mModel.getOptions(pKey);
                comboModel = new DefaultComboBoxModel(possibleValues);
            }                                    
            JComboBox combo = new JComboBox(comboModel);
            
            combo.setEditable(false);                                    
            comp = combo;
        }
        else if (type.equals(FormData.TYPE_BOOLEAN)) {
            ArrayList possibleValues = new ArrayList(); 
            if (editable == false) {
                possibleValues.add(new Boolean(ConversionUtils.anyToBoolean(mModel.get(pKey))));
            }
            else {
                possibleValues.add(Boolean.TRUE);
                possibleValues.add(Boolean.FALSE);
            }
                                                
            JComboBox combo = new JComboBox(new Vector(possibleValues));
            combo.setEditable(false);            
            
            comp = combo;
        }
        else if(type.equals(FormData.TYPE_FILE)) { 
            comp = new FileComponent();                        
        }
        else {
            // string, int, or float, or non-editable enum, we presume
            Document doc = mModel.getDocumentForTextField(pKey);                        
            if (doc != null) {                        
                comp = new JTextField(doc, null, 0);
            }
            else {
                comp = new JTextField();
            }
        }     
        
        if (comp instanceof JTextField) {
            ((JTextField)comp).getDocument().addDocumentListener(new TextFieldListener(pKey));
        }
        else if (comp instanceof FileComponent) {
            FileComponent fileComponent = (FileComponent)comp;
            fileComponent.mPathTextField.getDocument().addDocumentListener(new TextFieldListener(pKey));            
        }
        else if (comp instanceof JComboBox) {
            ((JComboBox)comp).addActionListener(new ComboBoxListener(pKey));
        }
        else {
            Utils.die("Unable to create listener for field: "+pKey);
        }
        
        comp.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // dbgMsg("Key pressed!");
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    guiToModel();
                    for (int i=0; i<mListeners.size(); i++) {
                        FormListener listener = (FormListener)mListeners.get(i);    
                        listener.formDataChanged();
                        listener.enterPressed();
                    }                            
                }
            }                
        });
                                        
        comp.setToolTipText(mModel.getTooltip(pKey));
          
        return comp;
    }
        
    
    public void oneFieldFromModelToGUI(String pKey) {        
        // dbgMsg("ModelToGUI: setting field "+key);
        Object val = mModel.get(pKey);
        // dbgMsg("ModelToGUI: setting field "+pKey+", val="+val);
        String type = mModel.getType(pKey);            
        boolean editable = mModel.isEditable(pKey);
        if (val == null) {
            val = mModel.getDefaultVal(pKey);    
        }            
        
        // JComponent comp = mFields[i];
        JComponent comp = (JComponent)mFieldsByKey.get(pKey);
        if (comp instanceof JTextField) {
            String valAsString = val.toString();
            // beautify val; this is mainly to get rid of ridiculously long doubles
            val = StringUtils.beautifyString(valAsString);                                                
            ((JTextField)comp).setText(valAsString);
            // dbgMsg("Setting text to field
            ((JTextField)comp).setEditable(editable);                                                
        }
        else if (comp instanceof JComboBox) {                
            // ((JComboBox)comp).setEditable(editable);
            if (type.equals(FormData.TYPE_BOOLEAN)) {
                Boolean booleanVal = new Boolean(ConversionUtils.anyToBoolean(val));
                ((JComboBox)comp).setSelectedItem(booleanVal);
            }
            else {                                           
                ((JComboBox)comp).setSelectedItem(val);                    
            }               
        }
        else if (comp instanceof FileComponent) {                                
            String valAsString = val.toString();
            FileComponent fileComp = (FileComponent)comp;                                                                                                
            fileComp.mPathTextField.setText(valAsString);                
            fileComp.mPathTextField.setEditable(editable);                               
        }
        else {
            throw new RuntimeException("Cannot handle this kind of component: "+comp+"!!!!!!");
        }
    }
    
    public void modelToGUI() {
        // dbgMsg(""+this+": starting modelToGui()");
        mMovingDataFromModelToGUI = true;
        String[] keys = mModel.getAllKeys();
        for (int i=0; i<keys.length; i++) {
            String key = keys[i];
            oneFieldFromModelToGUI(key);            
        }        
        mMovingDataFromModelToGUI = false;                
        revalidate();                        
    }        
    
    private void fieldValueChanged(String pFieldName) {
        // Logger.info("fieldValueChanged: "+pFieldName);
        // dbgMsg("mMovingDataFromModelToGUI: "+mMovingDataFromModelToGUI);
        
        if (!mMovingDataFromModelToGUI) {
            // Logger.info("Moving data from model to gui; maybe, just maybe, we may need to notify someone...");
            // dbgMsg("Notifying form listeners...");
            
            // update dependent fields...
            String dependentField = mModel.getDependentField(pFieldName);            
            if (dependentField != null) {
                // Logger.info("Updating dependent field: "+dependentField);
                guiToModel();                                    
                mModel.updateDependentField(dependentField);
                oneFieldFromModelToGUI(dependentField);
            }

            // notify listeners...            
            for (int i=0; i<mListeners.size(); i++) {
                FormListener listener = (FormListener)mListeners.get(i);                
                listener.formDataChanged();
                listener.formFieldValueChanged(pFieldName);                                                               
            }
        }
        else {
            // Logger.info("Moving data from model to gui, nobody is notified.");
        }
    }
    
    private class ComboBoxListener implements ActionListener {
        private String mFieldName;
        
        private ComboBoxListener(String pFieldName) {
            mFieldName = pFieldName;
        }            
        
        public void actionPerformed(ActionEvent e) {
            // dbgMsg("Combobox selection changed");
            fieldValueChanged(mFieldName);
        }
    }
    
    private class TextFieldListener implements DocumentListener {
        
        private String mFieldName;
        
        private TextFieldListener(String pFieldName) {
            mFieldName = pFieldName;
        }        
        
        public void insertUpdate(DocumentEvent e) {
            fieldValueChanged(e);
        }
        public void removeUpdate(DocumentEvent e) {
            fieldValueChanged(e);
        }
        public void changedUpdate(DocumentEvent e) {
            fieldValueChanged(e);
        }
                
        private void fieldValueChanged(DocumentEvent e) {
            FormPanel.this.fieldValueChanged(mFieldName);
            // displayEditInfo(e);            
        }
        
        /*
        private void displayEditInfo(DocumentEvent e) {
            Document doc = (Document)e.getDocument();
            int changeLength = e.getLength();
            // String msg = e.getType().toString() + ": "
                // + changeLength + " character"
                // + ((changeLength == 1) ? ". " : "s. ")
                // + " Text length = " + doc.getLength()
                // + "." + "\n";
            // dbgMsg(msg);
        }
        */
    } 

    public String toString() {
        return "FormPanel, model="+mModel.getFormName();         
    }
    
    private void setSize() {
        if (mFields == null || mLabels == null) {
            return;
        }
        int h1 = calculateFieldsMinimumHeight();
        int h2 = calculateLabelsMinHeight();
        int minh = h1<h2 ? h2 : h1;
        // dbgMsg("calculated min h: "+minh);
        Border border = getBorder();
        int extrah = 0;
        if (border != null) {
            Insets insets = getBorder().getBorderInsets(this);
            extrah = insets.top+insets.bottom;
        }
        // setMinimumSize(new Dimension(DEFAULT_WIDTH, minh+extrah));
        setPreferredSize(new Dimension(DEFAULT_WIDTH, minh+extrah));        
    }
    
    public void revalidate() {        
        setSize();
        super.revalidate();    
    }
    
    private int calculateFieldsMinimumHeight() {
        int result = 0;
        for(int i=0; i<mFields.length; i++) {
            result += mFields[i].getPreferredSize().getHeight();
            result += Y_PAD;
        }         
        return result;                          
    }
    
    public Dimension getMinimumSize() {
        Dimension minSize = super.getMinimumSize();
        // dbgMsg("minsize: "+minSize);
        return minSize;
    }
    
    public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        // dbgMsg("prefsize: "+prefSize);
        return prefSize;
    }
    
    private int calculateLabelsMinHeight() {
        int result = 0;        
        for(int i=0; i<mLabels.length; i++) {
            result += mLabels[i].getPreferredSize().getHeight();
            result += Y_PAD;
        }         
        return result;    
    }
    
    private void createBorderIfNeeded() {
        LineBorder lineBorder = new LineBorder(Color.black);
        String formName = mModel.getFormName();
        if (formName != null) {
            TitledBorder border = new TitledBorder(lineBorder, formName);
            setBorder(border);
        }            
    }
   
   private void oneFieldFromToGuiToModel(String pKey) {
       // JComponent comp = mFields[i];       
       JComponent comp = (JComponent)mFieldsByKey.get(pKey);         
       Object val=null;
       // Object defaultVal = mModel.getDefaultVal(pKey);
       if (comp instanceof JTextField) {                                                
           val = ((JTextField)comp).getText();                                
       }
       else if (comp instanceof JComboBox) {                
           val = ((JComboBox)comp).getSelectedItem();
       }  
       else if (comp instanceof FileComponent) {                
           val = new File(((FileComponent)comp).mPathTextField.getText());
       }                                                        
       else {
           throw new RuntimeException("Cannot handle component: "+comp);
       }                
       if (val != null) {
           mModel.put(pKey, val);  
       }
   }
   
    public void guiToModel() { 
        String[] keys = mModel.getAllKeys();
        for (int i=0; i<keys.length; i++) {            
            oneFieldFromToGuiToModel(keys[i]);            
        }        
    }    
    
    public void addFormListener(FormListener pListener) {        
        mListeners.add(pListener);    
    }
    
    public void removeFormListener(FormListener pListener) {        
        mListeners.remove(pListener);    
    }
    
//    public void setEditable(boolean pFlag) {
//        // todo...            
//    }
    
    private class FileComponent extends JPanel {        
        
        JButton mBrowseButton;
        JTextField mPathTextField;
                
        FileComponent() {
            mBrowseButton = new JButton("Browse");
            mPathTextField = new JTextField();
            mPathTextField.setPreferredSize(new Dimension(200, 20));            
            mBrowseButton.addActionListener(new BrowseButtonActionListener(this));
            
            add(mPathTextField);
            add(mBrowseButton);
        }                               
    }
    
    private class BrowseButtonActionListener implements ActionListener  {
        
        private FileComponent mFileComponent;
        
        private BrowseButtonActionListener(FileComponent pFileComponent) {
            mFileComponent = pFileComponent;
        }            
        
        public void actionPerformed(ActionEvent e) {
            UserQueryListener fileSelectionListener = new UserQueryListener() {
                public void onCancel() { /* no action */ }      
                public void onOk(FormData pForm) { throw new RuntimeException("Not implemented, you fool!"); }    
                public void onOk(Object pValue) {
                    File file = (File)pValue;                                                
                    String filename = ""+file;
                    mFileComponent.mPathTextField.setText(filename);                                                                                                                                                
                }
            };                        
            
            GuiUtils.getMain().getFileSelection(new File("/home/leronen/"), // initial dir 
                                                "select file", // title
                                                fileSelectionListener, // our listener will handle "OK" 
                                                false); // window not maximized            
        }
    }        
        
    
}


