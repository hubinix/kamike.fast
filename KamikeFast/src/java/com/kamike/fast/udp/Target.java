/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.google.common.collect.Queues;
import com.kamike.fast.FastConfig;
import com.kamike.fast.FastInst;
import com.kamike.fast.misc.UuidUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Target implements Runnable {

    private FastInst inst;
    private long high;
    private long low;
    private String fileName;
    private long position;
    private RandomAccessFile file;
    private RandomAccessFile cfgFile;
    private String cfgFileName;
    private long expiredTime = 600000L;
    private boolean finish;
    private boolean broken;
    private long sleep;

    private long updateDate;
    private int packet;
    private ConcurrentLinkedQueue<Window> queue;

    private Bow bow;
    private Header header;

    public Target(Bow bow, long high, long low) {
        this.header = new Header();
        fileName = UuidUtils.base58Uuid(high, low);
        cfgFileName = fileName + FastConfig.ConfigFileExtension;
        this.updateDate = System.currentTimeMillis();
        queue = Queues.newConcurrentLinkedQueue();
        this.sleep = 1000;
        this.bow = bow;
    }

    public Iterator<Window> Windows() {
        
        return queue.iterator();
    }
    public void removeWindow(Window window)
    {
        this.queue.remove(window);
    }

    public void open() {
        try {

            file = new RandomAccessFile(fileName, "rw");

            position = 0L;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Target.class.getName()).log(Level.SEVERE, null, ex);
            file = null;
        }
        Path cfgFilePath = FileSystems.getDefault().getPath(cfgFileName);

        boolean pathExists = Files.exists(cfgFilePath,
                new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
        if (pathExists) {
            try {
                cfgFile = new RandomAccessFile(cfgFileName, "rw");
                position = cfgFile.readLong();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Target.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Target.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                cfgFile = new RandomAccessFile(cfgFileName, "rw");
                cfgFile.writeLong(position);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Target.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Target.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void update() {

        this.updateDate = System.currentTimeMillis();
        this.setPacket(0);
    }

    public void close() {
        try {
            this.file.close();

        } catch (IOException ex) {
            Logger.getLogger(Target.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try {

            this.cfgFile.close();
        } catch (IOException ex) {
            Logger.getLogger(Target.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write(byte[] buffer) {
        try {
            if (file == null) {
                this.open();
            }
            file.write(buffer);
            this.position = file.getFilePointer();

        } catch (IOException ex) {
            Logger.getLogger(Target.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write(long windowId, byte[] buffer) {
        long pos = windowId * FastConfig.WindowLength;

        if (file == null) {
            this.open();
        }
        if (pos != position) {
            try {
                this.file.seek(pos);
                file.write(buffer);
                this.position = file.getFilePointer();
                this.cfgFile.seek(0);
                this.cfgFile.writeLong(position);

            } catch (IOException ex) {
                Logger.getLogger(Target.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                file.write(buffer);
                this.position = file.getFilePointer();
                this.cfgFile.seek(0);
                this.cfgFile.writeLong(position);
            } catch (IOException ex) {
                Logger.getLogger(Target.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @return the high
     */
    public long getHigh() {
        return high;
    }

    /**
     * @param high the high to set
     */
    public void setHigh(long high) {
        this.high = high;
    }

    /**
     * @return the low
     */
    public long getLow() {
        return low;
    }

    /**
     * @param low the low to set
     */
    public void setLow(long low) {
        this.low = low;
    }

    @Override
    public void run() {
        try {
            while (!this.finish) {
                try {
                    Thread.sleep(sleep);
                    if (!this.broken) {

                        if ((System.currentTimeMillis() - this.updateDate) < this.expiredTime) {

                            this.broken = false;
                        } else {
                            this.broken = true;
                        }
                        //这里需要发送window里面的东西了。循环队列.
                        Window window = this.queue.poll();
                        if (window == null) {

                        } else {

                            this.report(window);
                            //重新插回去，进行循环
                            this.queue.add(window);

                        }

                    }
                    sleep = 1000;

                } catch (InterruptedException ex) {
                    Logger.getLogger(Archer.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }

            this.close();
            this.bow.close();
        } catch (IOException ex) {
            Logger.getLogger(Target.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void report(Window window) {
        try {

            byte[] bytes = window.getHits();
            header.setId(0);
            header.setWindow(window.getId());
            header.setLength(bytes.length);
            header.setSize(0);
            header.setLow(this.low);
            header.setHigh(this.high);
            header.setType(PacketType.Target.ordinal());
            byte[] headData = header.data();
            byte[] packetData = new byte[headData.length + bytes.length];
            System.arraycopy(headData, 0, packetData, 0, headData.length);
            System.arraycopy(bytes, 0, packetData, headData.length, bytes.length);
            bow.send(packetData);
        } catch (IOException ex) {
            Logger.getLogger(Archer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
     * @param bow the bow to set
     */
    public void setBow(Bow bow) {
        this.bow = bow;
    }

    public Bow getBow() {
        return bow;
    }

}
