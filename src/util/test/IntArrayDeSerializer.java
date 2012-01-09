package util.test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import util.StringUtils;

// import util.Range;



/** Output integers read from command line in serialized int[] format */
public class IntArrayDeSerializer {
    
    public static void main(String[] args) throws Exception {
        
        ByteArrayInputStream sbIs = new ByteArrayInputStream(args[0].getBytes());
        ObjectInputStream ois = new ObjectInputStream(sbIs);
        int[] reconstructedData = (int[])ois.readObject();
        
        System.out.println(StringUtils.arrayToString(reconstructedData, " "));
                        
//        for (int i=0; i<reconstructedData.length; i++) {
//            System.out.println(reconstructedData[i]);
//        }
        
    }
}
