package util.algorithm.frequentsets;

import java.util.List;

import util.StringUtils;

public class Transaction {
    List<Item> itemList;
    int ind;
    
    public Transaction(List<Item> pItemList, int pInd) {
        itemList = pItemList;
        ind = pInd;
    }
    
    public String toString() {
        return "t"+ind+" "+StringUtils.listToString(itemList, " ");
    }
    
    
}
