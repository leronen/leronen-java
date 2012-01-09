package gui.plot;


/** interface that enables a Plot1D class to plot a histogram of 1D data */
public interface Data1D extends Data {    
    
    public double getValueAt(int pInd);        
    public double getMaxVal();
    public double getMinVal();
            
}