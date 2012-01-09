package util.test;

/**
 * Testing, whether some horrible loss of precision using log-transformation.
 * At least this trivial test does not reveal any such horrors.
 */
public class LogTest {

    public static void main(String[] args) {
        double original = 0.1;
        while (original > 0.0000000000000001) {
            original = original * 0.99;
            double log = Math.log(original);
            double doublyTransformed = Math.exp(log);
            System.out.println(""+original+"\t"+log+"\t"+doublyTransformed);
        }                                          
    }
    
    
}
