package gui;

import java.awt.event.KeyEvent;

public class KeyUtils {
    
    /** For debugging purposes */
    public static void displayInfo(KeyEvent e, util.dbg.ILogger log) {
      // One should only rely on the key char if the event
      // is a key typed event.
      int id = e.getID();
      String keyString;
      if (id == KeyEvent.KEY_TYPED) {
          char c = e.getKeyChar();
          keyString = "key character = '" + c + "'";
      } else {
          int keyCode = e.getKeyCode();
          keyString = "key code = " + keyCode
                  + " ("
                  + KeyEvent.getKeyText(keyCode)
                  + ")";
      }
      
      int modifiersEx = e.getModifiersEx();
      String modString = "extended modifiers = " + modifiersEx;
      String tmpString = KeyEvent.getModifiersExText(modifiersEx);
      if (tmpString.length() > 0) {
          modString += " (" + tmpString + ")";
      } else {
          modString += " (no extended modifiers)";
      }
      
      String actionString = "action key? ";
      if (e.isActionKey()) {
          actionString += "YES";
      } else {
          actionString += "NO";
      }
      
      String locationString = "key location: ";
      int location = e.getKeyLocation();
      if (location == KeyEvent.KEY_LOCATION_STANDARD) {
          locationString += "standard";
      } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
          locationString += "left";
      } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
          locationString += "right";
      } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
          locationString += "numpad";
      } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
          locationString += "unknown";
      }
                  
      log.info(keyString);
      log.info(modString);
      log.info(actionString);
      log.info(locationString);           
    }
}
