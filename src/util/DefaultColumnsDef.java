package util;

import java.util.List;

import util.collections.IndexMap;

public class DefaultColumnsDef implements ColumnsDef {
    private IndexMap<String> data;
    
    public DefaultColumnsDef(List<String> columnNames) {
        data = new IndexMap<String>(columnNames);
    }

    @Override
    public List<String> getColumnNames() {
        return data.asList();
    }

    @Override
    public Integer getColumnIndex(String columnName) {
        return data.getIndex(columnName);
    }
    
    
}

