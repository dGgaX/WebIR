/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.fetchremotes.gui;

import de.abring.rxtx.SerialConnection;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Karima
 */
public class Remotes extends javax.swing.JInternalFrame {

    private final DefaultTableModel model;
    private final FetchRemotes parent;
    private SerialConnection serialConnection;
    private final File filename;
    
    /**
     * Creates new form Remotes
     * @param parent
     * @param serialConnection
     * @param filename
     */
    public Remotes(FetchRemotes parent, SerialConnection serialConnection, File filename) {
        initComponents();
        model = new MyDefaultTableModel(parent.getKeys().toArray(), 0);
        this.jTbl.setModel(model);
        this.jTbl.setDragEnabled(true);
        this.jTbl.setDropMode(DropMode.INSERT_ROWS);
        this.jTbl.setTransferHandler(new TransferHandler() {

            public boolean canImport(TransferSupport support) {
                if (!support.isDrop()) {
                    return false;
                }

                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return false;
                }

                return true;
            }

            public boolean importData(TransferSupport support) {
        
                if (!canImport(support)) {
                    return false;
                }

                JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();

                int row = dl.getRow();

        
                String data;
                try {
                  data = (String) support.getTransferable().getTransferData(
                      DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException e) {
                  return false;
                } catch (IOException e) {
                  return false;
                }

                String[] rowData = data.split(",");
                model.insertRow(row, rowData);

                Rectangle rect = jTbl.getCellRect(row, 0, false);
                if (rect != null) {
                  jTbl.scrollRectToVisible(rect);
                }

                return true;
            }
        }); 
        
        this.jTbl.changeSelection(0, 1, false, false);
        this.parent = parent;
        this.serialConnection = serialConnection;
        this.filename = filename;
    }
    
    public JSONObject getJSONObject() {
        JSONObject all = new JSONObject();
        
        for (int r = 0; r < this.jTbl.getRowCount(); r++) {

            JSONObject remote = new JSONObject();
    
            for (int c = 1; c < this.jTbl.getColumnCount(); c++) {
                
                JSONArray command = (JSONArray) this.jTbl.getValueAt(r, c);
                
                remote.put(this.jTbl.getColumnName(c), command);
                
            }
            
            all.put((String) this.jTbl.getValueAt(r, 0), remote);
            
        }
        
        return all;
    }
    
    public JSONArray getJSONArray() {
        JSONArray all = new JSONArray();
        
        for (int c = 1; c < this.jTbl.getColumnCount(); c++) {

            JSONArray remote = new JSONArray();
            
            for (int r = 0; r < this.jTbl.getRowCount(); r++) {
                
                JSONArray command = (JSONArray) this.jTbl.getValueAt(r, c);
                
                remote.put(command);
                
            }
            
            all.put(remote);
            
        }
        
        return all;
    }
    
    public void setTextAtSelection(JSONArray input) {
        int row = this.jTbl.getSelectedRow();
        int column = this.jTbl.getSelectedColumn();
        
        if (column == 0) {
            column++;
            this.jTbl.changeSelection(row, column, false, false);
        }
        this.jTbl.setValueAt(input, row, column);
        column++;
        if (column >= this.jTbl.getColumnCount()) {
            row++;
            column = 0;
        }
        if (row >= this.jTbl.getRowCount()) {
            row = 1;
        }
        this.jTbl.changeSelection(row, column, false, false);
    }

    public void setTextAtColumn(JSONArray input, int column) {
        int row = this.jTbl.getSelectedRow();
        
        if (column == 0) {
            column++;
            this.jTbl.changeSelection(row, column, false, false);
        }
        this.jTbl.setValueAt(input, row, column);
        column++;
        if (column >= this.jTbl.getColumnCount()) {
            row++;
            column = 0;
        }
        if (row >= this.jTbl.getRowCount()) {
            row = 1;
        }
        this.jTbl.changeSelection(row, column, false, false);
    }

    public final void addRemote() {
        String response = JOptionPane.showInputDialog(this.getFRParent(),
            "What is the name of the Remote?",
            "Enter the name",
            JOptionPane.QUESTION_MESSAGE);
        Object[] newRowObject = new Object[model.getColumnCount()];
        if (newRowObject.length > 0 && response != null && !response.isEmpty())
            newRowObject[0] = response;
        model.addRow(newRowObject);
        this.jTbl.changeSelection(this.jTbl.getRowCount() - 1, 1, false, false);
    }
    
    public final void addThisRemote(String response, JSONObject remote) {
        
        Object[] newRowObject = new Object[model.getColumnCount()];
        if (newRowObject.length > 0 && response != null && !response.isEmpty())
            newRowObject[0] = response;
        
        for (int i = 1; i < this.jTbl.getColumnCount(); i++) {
            String columnName = this.jTbl.getColumnName(i);
            if (remote.has(columnName)) {
                newRowObject[i] = remote.get(columnName);
            }
        }
        
        model.addRow(newRowObject);
        this.jTbl.changeSelection(this.jTbl.getRowCount() - 1, 1, false, false);
    }
    
    public final void removeRemote() {
        int index = this.jTbl.getSelectedRow();
        if (index < 0)
            return;
        this.jTbl.changeSelection(Math.max(0, index - 1), 0, false, false);
        model.removeRow(index);
    }
    
    public final void wizard() {
        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < this.jTbl.getColumnCount(); i++) {
            String columnName = this.jTbl.getColumnName(i);
            columnNames.add(columnName);
        }
        Wizard wizard = new Wizard(this, true, columnNames.toArray(), this.serialConnection);
        wizard.setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSclPneTbl = new javax.swing.JScrollPane();
        jTbl = new javax.swing.JTable();
        jPne = new javax.swing.JPanel();
        jBtnAddRemote = new javax.swing.JButton();
        jBtnRemoveRemote = new javax.swing.JButton();
        jBtnWizard = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Remote");

        jTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTblMouseClicked(evt);
            }
        });
        jSclPneTbl.setViewportView(jTbl);

        jBtnAddRemote.setText("Add");
        jBtnAddRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnAddRemoteActionPerformed(evt);
            }
        });

        jBtnRemoveRemote.setText("Remove");
        jBtnRemoveRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnRemoveRemoteActionPerformed(evt);
            }
        });

        jBtnWizard.setText("Wizard");
        jBtnWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnWizardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPneLayout = new javax.swing.GroupLayout(jPne);
        jPne.setLayout(jPneLayout);
        jPneLayout.setHorizontalGroup(
            jPneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBtnWizard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBtnRemoveRemote)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnAddRemote)
                .addContainerGap())
        );
        jPneLayout.setVerticalGroup(
            jPneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPneLayout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnAddRemote)
                    .addComponent(jBtnRemoveRemote)
                    .addComponent(jBtnWizard))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPne, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSclPneTbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSclPneTbl, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPne, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnAddRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnAddRemoteActionPerformed
        addRemote();
    }//GEN-LAST:event_jBtnAddRemoteActionPerformed

    private void jBtnRemoveRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnRemoveRemoteActionPerformed
        removeRemote();
    }//GEN-LAST:event_jBtnRemoveRemoteActionPerformed

    private void jBtnWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnWizardActionPerformed
        wizard();
    }//GEN-LAST:event_jBtnWizardActionPerformed

    private void jTblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTblMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            int row = this.jTbl.getSelectedRow();
            int column = this.jTbl.getSelectedColumn();
            if (column == 0) {
                String response = JOptionPane.showInputDialog(this.getFRParent(),
                "What is the name of the Remote?",
                "Enter the name",
                JOptionPane.QUESTION_MESSAGE);
                if (response != null && !response.isEmpty())
                    this.jTbl.setValueAt(response, row, column);
            } else {
                Object object = this.jTbl.getValueAt(row, column);
                if (!(object instanceof JSONArray))
                    object = new JSONArray("[0,0,0]");
                Edit editor = new Edit(this.getFRParent(), true, (JSONArray) object);
                editor.setVisible(true);
                if (editor.getJSONArray() == null)
                    return;
                this.jTbl.setValueAt(editor.getJSONArray(), row, column);
            }
        }
    }//GEN-LAST:event_jTblMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnAddRemote;
    private javax.swing.JButton jBtnRemoveRemote;
    private javax.swing.JButton jBtnWizard;
    private javax.swing.JPanel jPne;
    private javax.swing.JScrollPane jSclPneTbl;
    private javax.swing.JTable jTbl;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the serialConnection
     */
    public SerialConnection getSerialConnection() {
        return serialConnection;
    }

    /**
     * @param serialConnection the serialConnection to set
     */
    public void setSerialConnection(SerialConnection serialConnection) {
        this.serialConnection = serialConnection;
    }

    /**
     * @return the parent
     */
    public FetchRemotes getFRParent() {
        return parent;
    }

    /**
     * @return the jTbl
     */
    public javax.swing.JTable getjTbl() {
        return jTbl;
    }

    /**
     * @return the filename
     */
    public File getFilename() {
        return filename;
    }
}
