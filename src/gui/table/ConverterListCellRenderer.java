package gui.table;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import util.converter.Converter;

public class ConverterListCellRenderer<T> extends DefaultListCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4251230711997129055L;
	private Converter<T,String> mConverter;
    
    public ConverterListCellRenderer(Converter<T,String> pConverter) {
        mConverter = pConverter;
    }
    
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) 
    {
        return super.getListCellRendererComponent(
            list, 
            mConverter.convert((T)value),
            index,
            isSelected,
            cellHasFocus);
            
    }
}
