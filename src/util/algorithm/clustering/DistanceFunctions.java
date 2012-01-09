package util.algorithm.clustering;

import java.util.Arrays;
import java.util.List;

import util.ConversionUtils;
import util.Initializable;
import util.converter.ObjectToStringConverter;

public enum DistanceFunctions {    
    DOT_PRODUCT("dot_product", DotProductDistanceFunction.class);    

    String mName;
    Class mImplClass;
    
    private DistanceFunctions(String pName, 
                              Class pImplClass) {
        mName = pName;
        mImplClass = pImplClass;
    }

    public static IDistanceFunction getImpl(String pName, 
                                            String pParam) throws Exception {
        for (DistanceFunctions dfWrapper: DistanceFunctions.values()) {
            if (dfWrapper.mName.equals(pName)) {
                IDistanceFunction d = (IDistanceFunction)dfWrapper.mImplClass.newInstance();
                ((Initializable)d).init(pParam);
                return d;
            }
        }
        throw new RuntimeException("No such distance function: " + pName);
    }
            

    public String toString() {
        return mName;
    }

    public static List<String> names() {
        return ConversionUtils.convert(Arrays.asList(DistanceFunctions.values()),
                new ObjectToStringConverter());
    }


}
