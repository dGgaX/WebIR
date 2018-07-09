/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.fetchremotes.gui.dialogues;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author Karima
 */
public class FileIO {
    private static final String FILE_ENDING = ".json";
    public static final File openJSONFile(Component frame, String workingDir) {
        JFileChooser chooser = new JFileChooser(workingDir);
        javax.swing.filechooser.FileFilter PDFFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory())
                    return true;
                return f.getName().toLowerCase().endsWith(FILE_ENDING);
            }
            @Override
            public String getDescription() {
                return "JSON-Dateien";
            }
        };
        javax.swing.filechooser.FileFilter AllFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return true;
            }
            @Override
            public String getDescription() {
                return "Alle Dateien";
            }
        };
        chooser.setDialogTitle("Remote öffnen");
        chooser.addChoosableFileFilter(AllFilter);
        chooser.setFileFilter(PDFFilter);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { 
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }
    public static final File saveJSONFile(Component frame, File fileName, String workingDir) {
        JFileChooser chooser = new JFileChooser(workingDir);
        javax.swing.filechooser.FileFilter PDFFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory())
                    return true;
                return f.getName().toLowerCase().endsWith(FILE_ENDING);
            }
            @Override
            public String getDescription() {
                return "JSON-Dateien";
            }
        };
        javax.swing.filechooser.FileFilter AllFilter = new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return true;
            }
            @Override
            public String getDescription() {
                return "Alle Dateien";
            }
        };
        chooser.setDialogTitle("Remote speichern");
        chooser.addChoosableFileFilter(AllFilter);
        chooser.setFileFilter(PDFFilter);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setSelectedFile(fileName);
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) { 
            File saveFile = chooser.getSelectedFile();
            if (!saveFile.getName().endsWith(FILE_ENDING)) {
                saveFile = new File(saveFile.getAbsolutePath() + FILE_ENDING);
            }
            try {
                if (saveFile.exists() && !MSG.msgOverwriteFile(frame, saveFile.getCanonicalPath())) {
                    return null;
                }
            } catch (IOException ex) {
                Logger.getLogger(FileIO.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return saveFile;
        } else {
            return null;
        }
    }
    
}
