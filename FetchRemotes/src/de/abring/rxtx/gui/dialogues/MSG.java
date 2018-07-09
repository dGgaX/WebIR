/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.rxtx.gui.dialogues;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author Karima
 */
public class MSG {
    /**
     * msgSaveAborted
     * @param frame
     * @param file 
     */
    public static final void msgSaveAborted(Component frame, String file) {
        JOptionPane.showMessageDialog(frame, "Achtung, die Datei \"" + file + "\" konnte nicht gespeichert werden.", "Hinweis", JOptionPane.OK_OPTION);
    }
    
    public static final boolean msgConnectionWillBeLost(Component frame) {
        return JOptionPane.showConfirmDialog(null, "Your current connection will be lost?", "Question", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
