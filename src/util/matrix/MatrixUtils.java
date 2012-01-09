package util.matrix;

import util.*;
import util.converter.*;

import java.util.*;

public final class MatrixUtils {
                
    // convert a map to a matrix.
    // the values of the map will be put to the matrix, with the
    // matrix indices specified by the map keys, as converted by pIndexMapper                 
    public static Matrix makeFromMap(Map pMap, IndexMapper pIndexMapper) {
        Converter keyToIndexConverter = new ToIndexConverter(pIndexMapper);
        Map byIndexMap = ConversionUtils.convertKeys(pMap, keyToIndexConverter, null);        
        Set indexSet = byIndexMap.keySet();   
        // dbgMsg("Index set: "+indexSet);             
        List rowList = ConversionUtils.convert(indexSet, new ListFieldExtractor(0));
        List colList = ConversionUtils.convert(indexSet, new ListFieldExtractor(1));
        // dbgMsg("Row list: "+rowList);
        // dbgMsg("Col list: "+colList);
        int numRows = MathUtils.max(ConversionUtils.integerCollectionToIntArray(rowList))+1;
        int numCols = MathUtils.max(ConversionUtils.integerCollectionToIntArray(colList))+1;
        // dbgMsg("numRows: "+numRows);
        // dbgMsg("numCols: "+numCols);
        Matrix result = new Matrix(numRows, numCols, false);
        result.fill(new Double(0.d));                
        Iterator indexIter = indexSet.iterator();
        while (indexIter.hasNext()) {
            Index index = (Index)indexIter.next();            
            Object val = byIndexMap.get(index);
            result.set_row_col(index.getRow(), index.getCol(), val);                                                     
        }                
        return result;
    }                                    
    
    public static class ToIndexConverter implements Converter {
        IndexMapper mIndexMapper;
        
        public ToIndexConverter(IndexMapper pIndexMapper) {
            mIndexMapper = pIndexMapper;
        }
            
        public Object convert(Object p) {
            return mIndexMapper.indexObjectToMatrixIndex(p);                
        }
    }        
        
        
}

