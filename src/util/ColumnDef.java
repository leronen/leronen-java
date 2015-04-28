package util;


/** Generic interface for a column of a database table or other such relation with named and typed columns */
public interface ColumnDef {
    public String getName();
    public ColumnType getType();
    /** Column index. Indexing starts from 0. */
    public int getIndex();
    /** Return null if column does not have a maximum length or the question of max length is ill-defined for column in question */
    public Integer getMaxLength();
}

