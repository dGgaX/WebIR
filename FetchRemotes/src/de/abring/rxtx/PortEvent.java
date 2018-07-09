/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.rxtx;

import java.awt.AWTEvent;

/**
 *
 * @author Bring
 */
public class PortEvent extends AWTEvent {
    
    public static final int STRING = 1;
    
    /**
     *
     * @param source the Point about Height and Timestamp
     */
    public PortEvent(String source) {
        super(source, PortEvent.STRING);
    }
    
}
