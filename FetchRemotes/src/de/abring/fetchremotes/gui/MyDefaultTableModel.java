/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.fetchremotes.gui;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Karima
 */
public class MyDefaultTableModel extends DefaultTableModel {
    
    private MyDefaultTableModel(int rows, int cols) { // constructor
        super(rows, cols);
    }

    public MyDefaultTableModel(Object[] os, int i) {
        super(os, i);
    }

    @Override
    public boolean isCellEditable(int row, int column) { // custom isCellEditable function
        return false;
    }
}