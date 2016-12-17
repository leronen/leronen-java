package util.sql;

import java.util.HashMap;
import java.util.List;

import util.CollectionUtils;
import util.StringUtils;

public class Row {

    private final TableDef mTableDef;
    private final HashMap<String, Object> mData;

    public Row(TableDef pTableDef) {
        mTableDef = pTableDef;
        mData = new HashMap(pTableDef.getNumColumns());
    }

    public Row createClone() {
        HashMap mapClone = new HashMap(mData);
        return new Row(mTableDef, mapClone);
    }

    public Row(TableDef pTableDef, HashMap<String, Object> pData) {
        mTableDef = pTableDef;
        mData = pData;
    }

    public TableDef getTableDef() {
        return mTableDef;
    }

    public String getString(String pColName) {
        return (String)mData.get(pColName);
    }

    public Integer getInt(String pColName) {
        return (Integer)mData.get(pColName);
    }

    public Boolean getIntAsBoolean(String pColName) {
        Integer intVal = (Integer)mData.get(pColName);
        if (intVal != null) {
            return intVal == 1;
        }
        else {
            return null;
        }
    }

    public void putInt(String pColName, Boolean pVal) {
        if (pVal == null) {
            mData.put(pColName, null);
        }
        else {
            mData.put(pColName, (pVal ? 1 : 0));
        }
    }


    public Double getDecimal(String pColName) {
        return (Double)mData.get(pColName);
    }

    public void put(String pColName, Integer pVal) {
        mData.put(pColName, pVal);
    }

    public void put(String pColName, Double pVal) {
        mData.put(pColName, pVal);
    }

    public void put(String pColName, String pVal) {
        mData.put(pColName, pVal);
    }

    public List asList() {
        return CollectionUtils.extractList(mData, mTableDef.getColumnNames());
    }

    @Override
    public String toString() {
        return StringUtils.collectionToString(asList(), " ");
    }
}
