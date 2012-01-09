package util.test;

/** A test for Fang */
public class Test {
    
    static boolean compare(Object object1, Object object2) {
        Relatable obj1 = (Relatable)object1;
        Relatable obj2 = (Relatable)object2;
        
        
        if (obj1.isLargerThan(obj2) > 0) {
            return true;
        }
        else {
            return false;
        }
            
    }
    
    public static void main(String[] args) {
        Employee empl_a = new Employee(24);
        Employee empl_c = new Employee(34);
        boolean result = compare(empl_a, empl_c);
        System.out.println(result);
    }
}


interface Relatable {
    public int isLargerThan(Relatable other);
}

class Employee implements Relatable {
    
    public Employee(int i) {
        data =i;
    }
    
    public int isLargerThan(Relatable other) {
        Employee otheree = (Employee) other;
        if (this.data > otheree.data) 
            return 1;
        else if (this.data <  otheree.data)
            return -1;
        else 
            return 0;               
    }
    
    private int data;
}

