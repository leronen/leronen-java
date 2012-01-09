package util.test;

import util.StringUtils;

public class ArrayTest {
    public static void main(String[] args) {
        double[] arr = new double[3];
        arr[0] = Double.NaN;
        System.out.println(StringUtils.arrayToString(arr));
    }
}
