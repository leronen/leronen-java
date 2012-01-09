package util.matrix;


/** This is sad but true: we are resorting to a factory of factories... what will it be next? */
public interface RowFactoryFactory {

    public RowFactory makeRowFactory(RowFormat pRowFormat);

}
