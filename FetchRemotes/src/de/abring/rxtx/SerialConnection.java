/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.abring.rxtx;

import gnu.io.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wohnzimmer
 */
public class SerialConnection {

    /**
     * @return the portListener
     */
    public List<PortListener> getPortListener() {
        return portListener;
    }
    
    private String lastCommand = "";
    private final List<PortListener> portListener = new ArrayList<>();
    private CommPortIdentifier selectedPort = null;
    private SerialPort selectedSerialPort;
    private OutputStream output = null;
    private InputStream input = null;
    private final HashSet<CommPortIdentifier> h =  getAvailableSerialPorts();
    private boolean connected = false;
    
    SerialPortEventListener selectedPortListener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent spe) {
            int data;
            byte[] buffer = new byte[1024];
            int len = 0;
            try {
                while ( ( data = input.read()) > -1 )
                {
                    if ( data == '\n' || data == '\r' ) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
            } catch (IOException ex) {
                Logger.getLogger(SerialConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            String inpString = new String(buffer,0,len);
            if (!inpString.isEmpty()) {
                executeTransmission(inpString);
            }
        }
        
    };
    
    public SerialConnection() {
    }
    
    /**
     * @return the selectedSerialPort
     */
    public SerialPort getSelectedSerialPort() {
        return selectedSerialPort;
    }
    
    public void writeToCommPort(String command) {
        if (isConnected() && output != null && !command.equals(lastCommand)) {
            try {
                output.write(command.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(SerialConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String[] getAvailableCommPortNames() {
        String[] cids = new String[h.toArray().length];
        int i = 0;
        for (Object cidObject : h.toArray()) {
            CommPortIdentifier cid = (CommPortIdentifier) cidObject;
            cids[i++] = cid.getName();
        }
        return cids;
    }
    
    public void selectCommPort(int i) {
        selectedPort = (CommPortIdentifier)h.toArray()[i];
    }
    
    public void selectCommPort(CommPortIdentifier cp) {
        selectedPort = cp;
    }
    
    public void selectCommPort(String cp) {
        for (Object cidObject : h.toArray()) {
            CommPortIdentifier cid = (CommPortIdentifier) cidObject;
            if (cid.getName().equals(cp)) {
                selectedPort = cid;
                return;
            }
        }
    }
    
    public boolean commPortSelected() {
        return (selectedPort != null);
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void connect() {
        System.out.println("Info: Connect.");
        if ( selectedPort.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            try {
                
                CommPort commPort = selectedPort.open(this.getClass().getName(),2000);

                if ( commPort instanceof SerialPort )
                {
                    selectedSerialPort = (SerialPort) commPort;
                    selectedSerialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                    
                    input = selectedSerialPort.getInputStream();
                    output = selectedSerialPort.getOutputStream();
                    
                    connected = true;
 
                    selectedSerialPort.addEventListener(selectedPortListener);
                    selectedSerialPort.notifyOnDataAvailable(true);
                }
                else
                {
                    System.out.println("Error: Only serial ports are handled by this example.");
                }
            } catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException ex) {
                System.out.println("Error: Port is in Use!");
            }
        }     
    }
    
    public void disconnect() {
        System.out.println("Info: Disconnect.");
        if (selectedSerialPort != null) {
            try {
                input.close();
                input = null;
                output.close();
                output = null;
                connected = false;
                selectedSerialPort.removeEventListener();
                
            } catch (IOException ex) {
                Logger.getLogger(SerialConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            selectedSerialPort.close();
        }
    }
    
    private static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
        HashSet<CommPortIdentifier> h = new HashSet<>();
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
            switch (com.getPortType()) {
            case CommPortIdentifier.PORT_SERIAL:
                try {
                    CommPort thePort = com.open("CommUtil", 50);
                    thePort.close();
                    h.add(com);
                } catch (PortInUseException e) {
                    System.out.println("Port, "  + com.getName() + ", is in use.");
                } catch (Exception e) {
                    System.err.println("Failed to open port " +  com.getName());
                }
            }
        }
        return h;
    }
    
    public void executeTransmission(String s) {
        PortEvent evt = new PortEvent(s);
        portListener.forEach((hl) -> {
            hl.inputDetected(evt);
        });
    }
}
