/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import java.net.DatagramPacket;

/**
 *
 * @author THiNk
 */
public class Result {

    public Result() {
        // this.buffer=new byte[FastConfig.PacketLength];
    }
    private byte[] buffer;
    private Header header;

    public void load(Header header, DatagramPacket packet) {
        if (packet == null) {
            return;
        }
        if (header == null) {
            return;
        }
        this.header = header;

        if (getBuffer() == null) {
            buffer = new byte[header.getLength()];
        }
        System.arraycopy(packet.getData(), packet.getOffset() + FastConfig.HeaderLength, getBuffer(), 0, this.header.getLength());

    }

    /**
     * @return the buffer
     */
    public byte[] getBuffer() {
        return buffer;
    }

}
