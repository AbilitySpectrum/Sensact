package lyricom.sensactConfig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO
 * Holds an array action ID - IR Code pairs.
 * One instance of the class holds all the pairs for
 * one TV type.
 * 
 * @author Andrew
 */
public class TVCodeMap {
    private class Mapping {
        final int actionID;
        final int IRCode;
        
        Mapping(int a, int c) {
            actionID = a;
            IRCode = c;
        }
    };
    
    List<Mapping> theList = new ArrayList<>();
    
    TVCodeMap() {}
    
    void addMapping(int actionID, int IRCode) {
        theList.add(new Mapping(actionID, IRCode));
    }
    
    int getActionID(int IRCode) {
        for(Mapping m: theList) {
            if (m.IRCode == IRCode) return m.actionID;
        }
        return 0;
    }
    
    int getIRCode(int actionID) {
        for(Mapping m: theList) {
            if (m.actionID == actionID) return m.IRCode;
        }
        return 0;
    }
}
