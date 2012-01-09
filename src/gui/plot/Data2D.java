package gui.plot;

/** interface that enables a Plot2D class to plot 2D data */
public interface Data2D extends Data {    
    
    public int getValueAt(int pX, int pY);        
    public int getMaxVal();
    public int getMinVal();  

    public int getNumYIndices();          
    public String getYIndexAt(int pInd);
}
