package util.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// import util.Range;



/** Shows how to serialize and deserialize a int array */
public class SerializeTest {
    
    public static void main(String[] args) throws Exception {

        // int[] data = new Range(1, 5000).asIntArr();
        int[] data = new int[args.length];        
        
        for (int i=0; i<args.length; i++) {
            data[i] = Integer.parseInt(args[i]);
        }
        
        ByteArrayOutputStream byteArrOs = new ByteArrayOutputStream();        
        ObjectOutputStream os = new ObjectOutputStream(byteArrOs);        
        os.writeObject(data);
        
        // System.out.println("Constructed byte array: "+byteArrOs.toString());
        
        ByteArrayInputStream sbIs = new ByteArrayInputStream(byteArrOs.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(sbIs);
        int[] reconstructedData = (int[])ois.readObject();
        
        System.out.println("Writing reconstructed data:");
                        
        for (int i=0; i<reconstructedData.length; i++) {
            System.out.println(reconstructedData[i]);
        }
        
    }
}
