/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;

/**
 *
 * @author THiNk
 */
public class Result {

    public Result() {
          this.buffer=new byte[FastConfig.DataLength];
    }
    private byte[] buffer;
    private Header header;

   
     public void load(Header header, byte[] packet) {
        if (packet == null) {
            return;
        }
        if (header == null) {
            return;
        }
        this.header = header;

        
        buffer = new byte[header.getLength()];
        
        System.arraycopy(packet,   FastConfig.HeaderLength, buffer, 0, this.header.getLength());

    }

    /**
     * @return the buffer
     */
    public byte[] getBuffer() {
        return buffer;
    }

}
