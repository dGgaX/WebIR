/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.fetchremotes.gui;

import de.abring.rxtx.*;
import javax.swing.DefaultListModel;
import org.json.JSONArray;

/**
 *
 * @author Karima
 */
public class Wizard extends javax.swing.JDialog {

    private String[] codes;
    private final SerialConnection serialConnection;
    DefaultListModel<String> model;
    
    private Object[] commands;
    private long endTimeMillis = System.currentTimeMillis() + 1000;
        
    boolean listeningForInput = true;
    
    private final Remotes parent;
    
    int column;
    int row;
    
    /**
     * Creates new form Wizard
     * @param parent
     * @param modal
     * @param commands
     * @param serialConnection
     */
    public Wizard(Remotes parent, boolean modal, Object[] commands, SerialConnection serialConnection) {
        super(parent.getFRParent(), modal);
        initComponents();
        this.parent = parent;
        this.model = new DefaultListModel<>();
        this.jLstCommands.setModel(model);
        this.serialConnection = serialConnection;
        this.commands = commands;
        column = 1;
        row = parent.getjTbl().getSelectedRow();
        parent.getjTbl().changeSelection(row, column, false, false);
        this.jLabel3.setText((String) commands[column]);
        this.waitThread.start();
        this.serialConnection.getPortListener().add(new PortListener()  {
            @Override
            public void inputDetected(PortEvent pvt) {
                doInput(pvt);
            }
        });
    }
    
    private void doInput(PortEvent pvt) {
        if (pvt.getID() == PortEvent.STRING && listeningForInput) {
            jLabel3.setText((String) commands[column]);
            String input = (String) pvt.getSource();
            model.add(0, input);
            if (model.size() >= 5) {
                if (model.get(0).equals(model.get(1)) &&
                    model.get(1).equals(model.get(2)) &&
                    model.get(2).equals(model.get(3)) &&
                    model.get(3).equals(model.get(4)) ) {
                    if (input.startsWith(parent.getFRParent().getIncoming())) {
                        input = input.substring(parent.getFRParent().getIncoming().length());
                        input = input.trim();
                        String[] commandParts = input.split(",");
                        if (commandParts.length >= 3) {

                            listeningForInput = false;
                            jLabel3.setText("STOP");
                            endTimeMillis = System.currentTimeMillis() + 1000;

                            JSONArray commandArray = new JSONArray();
                            commandArray.put(Long.parseLong(commandParts[0]));
                            commandArray.put(Long.parseLong(commandParts[1]));
                            commandArray.put(Long.parseLong(commandParts[2]));
                            parent.setTextAtColumn(commandArray, column);
                            model.clear();
                            column++;
                            if (column >= parent.getjTbl().getColumnCount()) {
                                closeWizard();
                            }
                        }
                    }
                }
            }
        }
    }

    
    private final Thread waitThread = new Thread() {
        boolean run = true;
        @Override
        public void run() {
            while(run) {
                try {
                    sleep(200);
                    if (System.currentTimeMillis() >= endTimeMillis) {
                        listeningForInput = true;
                        jLabel3.setText((String) commands[column]);
                    
                    }
                } catch(InterruptedException e) {
                    run = false;
                }
            }
        }
    };
    
    public void closeWizard() {
        waitThread.interrupt();
        listeningForInput = false;
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jSclPneLstCommands = new javax.swing.JScrollPane();
        jLstCommands = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jLabel1.setText("Please press the:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
        jLabel2.setText("-Button on your remote.");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Power");

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jSclPneLstCommands.setBorder(null);

        jLstCommands.setBackground(new java.awt.Color(240, 240, 240));
        jLstCommands.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jLstCommands.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLstCommandsMouseClicked(evt);
            }
        });
        jSclPneLstCommands.setViewportView(jLstCommands);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jSclPneLstCommands, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 196, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(106, 106, 106)
                    .addComponent(jSclPneLstCommands, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addGap(44, 44, 44)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLstCommandsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLstCommandsMouseClicked
//        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
//            if (this.jDesktop.getSelectedFrame() == null || !(this.jDesktop.getSelectedFrame() instanceof Remotes))
//            return;
//            Remotes remote = (Remotes)this.jDesktop.getSelectedFrame();
//            String[] commandParts = this.jLstCommands.getSelectedValue().split(",");
//            JSONArray commandArray = new JSONArray();
//            commandArray.put(Integer.parseInt(commandParts[0]));
//            commandArray.put(Integer.parseInt(commandParts[1]));
//            commandArray.put(Integer.parseInt(commandParts[2]));
//            remote.setTextAtSelection(commandArray);
//        }
    }//GEN-LAST:event_jLstCommandsMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        closeWizard();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList<String> jLstCommands;
    private javax.swing.JScrollPane jSclPneLstCommands;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the codes
     */
    public String[] getCodes() {
        return codes;
    }
}
