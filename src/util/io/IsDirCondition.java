package util.io;

import util.condition.*;

import java.io.*;

public class IsDirCondition implements Condition {

    public boolean fulfills(Object pObj) {
        File file = (File)pObj;
        return file.isDirectory();
    }

}
