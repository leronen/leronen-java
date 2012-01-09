package util.converter;

import util.*;

public interface StringToObjectWeightPairConverter <T> {

    public ObjectWeightPair<T> convertStringToObjectWeightPair(String pString);
}
