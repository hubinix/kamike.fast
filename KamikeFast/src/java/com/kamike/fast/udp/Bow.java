/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Bow {

    DatagramSocket socket;
  
    private InetAddress address;
    private int port;
    public Bow(InetAddress address, int port) {
        try {
            socket = new DatagramSocket();
            socket.connect(address, port);
          
        } catch (SocketException ex) {
            Logger.getLogger(Bow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void send(byte[] message) throws IOException {
        DatagramPacket packet
                = new DatagramPacket(message, message.length);
        socket.send(packet);
    }
      public void close() throws IOException {
        this.close();
    }

    /**
     * @return the address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
}
