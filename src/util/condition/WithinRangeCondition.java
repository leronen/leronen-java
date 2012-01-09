package util.condition;

import util.IOUtils;

public class WithinRangeCondition <T extends Number & Comparable<T>> implements Condition<T> {
	
	private T mStart;
	private T mEnd;
	
	/** 
	 *  @param pStart inclusive
	 *  @param pEnd exclusive
	 */
	public WithinRangeCondition(T pStart, T pEnd) {
		mStart = pStart;
		mEnd = pEnd;
	}
        
		
	public boolean fulfills(T pObj) {
		// return mStart <= pObj && pObj < mEnd; 
        return mStart.compareTo(pObj) <= 0 && pObj.compareTo(mEnd) < 0;
	}
    
    public static void main(String[] args) throws Exception {        
        WithinRangeCondition<Integer> condition = new WithinRangeCondition<Integer>(3,10);
        String[] data = IOUtils.readLineArray(System.in);        
        for (String s: data) {
            int i = Integer.parseInt(s);
            boolean fulfills = condition.fulfills(i);
            System.out.println(""+i+": "+fulfills);
        }
        
        
    }
}
