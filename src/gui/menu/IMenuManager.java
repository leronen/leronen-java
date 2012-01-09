package gui.menu;

import gui.action.CommandHandler;

import java.util.List;

import javax.swing.JButton;

// import javax.swing.*;

/** 
 *  Hopefully bridges the gap between old and new menumanager implementations,
 *  at least in some limited cases.
 */
public interface IMenuManager {

    public List<JButton> makeButtons(CommandHandler pCommandHandler, Iterable<String> pCommandIds);         
   
    // public JMenuBar getMenuBar();
    // public void refresh();        

}
