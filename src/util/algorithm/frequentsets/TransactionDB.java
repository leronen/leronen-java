package util.algorithm.frequentsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.IOUtils;
import util.StringUtils;

public class TransactionDB {          
    
    /** transaction id is the index */
    private List mTransactions  = new ArrayList();
    
    private Map<String, Item> mItemsByKey = new HashMap();
    
    /** item id is the index */
    private List<Item> mItems = new ArrayList();    
    
    
    
    public TransactionDB(String pFile) throws IOException {
        for (String line: IOUtils.readLineArray(pFile)) {
            addTransaction(line);
        }
    }
   
    public int getNumItems() {
        return mItems.size();
    }
    
    public List<Transaction> getTransactions() {
        return mTransactions;
    }
    
    public List<Item> getItems() {
        return mItems;
    }
    
    public Item getOrCreateItem(String pKey) {
        Item item = mItemsByKey.get(pKey);
        if (item == null) {
            int ind = mItems.size();
            item = new Item(pKey, ind);            
            mItemsByKey.put(pKey, item);
            mItems.add(item);
        }
        return item;        
    }
    
    public Transaction addTransaction(String pLine) {
        String[] tokens= StringUtils.fastSplit(pLine, ' ');
        List<Item> items = new ArrayList(tokens.length);
        
        for (String itemKey: tokens) {
            Item item = getOrCreateItem(itemKey);
            items.add(item);
        }
        
        Transaction t = new Transaction(items, mTransactions.size());
        mTransactions.add(t);
        return t;
    }
    
    public String toString() {
        return "Items:\n"+
               StringUtils.listToString(mItems)+"\n"+
               "\n"+
               "Transactions:\n"+
               StringUtils.listToString(mTransactions)+"\n";
        	   
        	   
    }
}
