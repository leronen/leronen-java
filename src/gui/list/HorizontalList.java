package gui.list;

import java.awt.Color;
import java.awt.Dimension;
//import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import util.collections.OneToOneBidirectionalMap;
import util.dbg.Logger;


/** Colorable, selectable horizontal list of labels */ 
public class HorizontalList<T> extends JPanel {
              
    /**
	 * 
	 */
	private static final long serialVersionUID = -2455667150959692445L;
	private Set<T> mSelectedItems;
    private List<JLabel> mLabels;
    private OneToOneBidirectionalMap<JLabel, T> mItemsByLabel;
    private OurMouseListener mMouseListener;
    private Listener mListener;
    
    public HorizontalList(Map<T, Color> pDataWithColoring) {
        this(pDataWithColoring.keySet(), pDataWithColoring);
    }
    
    public HorizontalList(Collection<T> pObjects) {
        this (pObjects, Collections.EMPTY_MAP);
    }
    
    public HorizontalList(Collection<T> pObjects, Map<T, Color> pColoring) {        
        mLabels = new ArrayList(pObjects.size());
        mSelectedItems = new HashSet(pObjects.size());
        mMouseListener = new OurMouseListener();
        mItemsByLabel = new OneToOneBidirectionalMap();
                
        // setLayout(new FlowLayout(FlowLayout.LEFT));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        for (T object: pObjects) {            
            Color color = pColoring.get(object);            
            JLabel label = new JLabel(""+object);
            int prefW = label.getPreferredSize().width + 4;
            int prefH = label.getPreferredSize().height +4;
            label.setPreferredSize(new Dimension(prefW, prefH));
                    
            label.setOpaque(true);
            if (color != null) {                
                label.setBackground(color);
            }            
            
            label.addMouseListener(mMouseListener);
            mLabels.add(label);
            mItemsByLabel.put(label, object);
            add(label);            
        }
    }
    
    public Set<T> getSelectedItems() {
        return Collections.unmodifiableSet(mSelectedItems);
    }
    
    public void setListener(Listener pListener) {
        mListener = pListener;
    }
    
    public interface Listener {
        public void selectionChanged();
    }
    
    /** Does not notify! */
    public void clearSelection() {
        for (T item: mSelectedItems) {
            JLabel label = mItemsByLabel.getInverse(item);
            label.setBorder(null);
        }
        mSelectedItems.clear();
    }
    
    private class OurMouseListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            Object source = e.getSource();
//            Logger.info("Mouse clicked at: "+e.getSource());
            if (!(source instanceof JLabel)) {
                return;
            }
            
            JLabel label = (JLabel)source;
                        
            T item = mItemsByLabel.get(label);
            
            if (item != null) {                
                
                if (e.isShiftDown()) {
                    if (mSelectedItems.contains(item)) {
                        label.setBorder(null);
                        mSelectedItems.remove(item);
                    }
                    else {
                        label.setBorder(new LineBorder(Color.black, 2));                    
                        mSelectedItems.add(item);
                    }
                }
                else {
                    // no shift, just set or unset selection 
                    if (mSelectedItems.contains(item) && mSelectedItems.size() == 1) {
                        label.setBorder(null);
                        mSelectedItems.remove(item);
                    }
                    else {
                        clearSelection();
                        mSelectedItems.add(item);
                        label.setBorder(new LineBorder(Color.black, 2));                                            
                    }
                }
                
                if (mListener != null) {
                    mListener.selectionChanged();
                }
            }
            else {
                Logger.warning("HorizontalList: unable to map mouse click to a item!!!");
            }
            
//            Logger.info("Corresponding item: "+mItemsByLabel.get(source));            
            
        }       
        
    }
        
}
