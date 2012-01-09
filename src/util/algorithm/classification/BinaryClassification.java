/**
 * 
 */
package util.algorithm.classification;

public enum BinaryClassification {        
    POS("p"),
    NEG("n");
       
    public String name;
        
    
    private BinaryClassification(String pName) {
        name = pName;
    }
    
    public static BinaryClassification getByString(String p) {
        if (p.equals(POS.name)) {
            return POS;
        }
        else if (p.equals(NEG.name)) {
            return NEG;
        }
        else {
            throw new RuntimeException("Illegal string rep for a classification: "+p);
        }          
    }
    
    public String toString() {
        return name;
    }
    
    
    
        
}