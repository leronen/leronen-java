package util.test;

import util.IOUtils;

public class DoubleTester {
    
    public static void main(String args[]) throws Exception {
        String[] lines = IOUtils.readLineArray(System.in);
        for (String line: lines) {
            double val = Double.parseDouble(line);
            System.out.println(val);
        }
    }
}
