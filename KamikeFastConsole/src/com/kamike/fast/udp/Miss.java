/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

/**
 *
 * @author THiNk
 */
public class Miss {

    private int packet;
    private long window;

    /**
     * @return the packet
     */
    public int getPacket() {
        return packet;
    }

    /**
     * @param packet the packet to set
     */
    public void setPacket(int packet) {
        this.packet = packet;
    }

    /**
     * @return the window
     */
    public long getWindow() {
        return window;
    }

    /**
     * @param window the window to set
     */
    public void setWindow(long window) {
        this.window = window;
    }

}
