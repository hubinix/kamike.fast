/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Archer implements Runnable {

    private long low;
    private long high;
    private long sleep;
    private String id;
    private Date createDate;
    private Quiver quiver;
    private Bow bow;
    private Header header;
    private volatile ArcherStatus status;

    private volatile long bandwidth = 100000L;

    private volatile long lastMaxBandwidth = 0L;

    private volatile long limitBandwidth = 1000000L;

    private long expiredTime = 600000L;

    private long updateDate;

    private ConcurrentLinkedQueue<Miss> queue;

    public Archer(Bow bow, Quiver quiver) {
        this.header=new Header();
        this.quiver = quiver;
        this.bow = bow;
        queue = new ConcurrentLinkedQueue();
        this.createDate = new Date(System.currentTimeMillis());
        this.updateDate = System.currentTimeMillis();
       
    }

    public void miss(Miss miss) {
        this.queue.add(miss);
    }

    public void update() {

        this.updateDate = System.currentTimeMillis();

    }

    public long getTransferred() {
        return this.quiver.getWindowId() * FastConfig.WindowLength;
    }

    public String getFileName() {
        return this.quiver.getFileName();
    }

    public int getPacket(long window) {
        if (this.quiver.getWindowId() == window) {
            return this.quiver.getPacketId();
        } else {
            if (this.quiver.getWindowId() < window) {
                return FastConfig.PacketInWindow;
            } else {
                return 0;
            }
        }
    }

    @Override
    public void run() {

        ///正常射击
        //补射
        while (this.getStatus() != ArcherStatus.Finish) {
            try {
                Thread.sleep(sleep);
                if (this.getStatus() != ArcherStatus.Sleeping) {

                    if ((System.currentTimeMillis() - this.updateDate) < this.expiredTime) {
                      
                        this.status = ArcherStatus.Shooting;
                    } else {
                        this.status = ArcherStatus.Sleeping;
                    }
                    Miss miss = this.queue.poll();
                    if (miss != null) {
                        this.shoot(miss);
                    } else {
                        if (!this.quiver.isFinish()) {

                            this.shoot();

                        }
                    }
                }
                sleep = (1000 * FastConfig.DataLength) / getBandwidth();

            } catch (InterruptedException ex) {
                Logger.getLogger(Archer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            this.quiver.close();
            this.bow.close();
        } catch (IOException ex) {
            Logger.getLogger(Archer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void shoot(Miss miss) {
        try {
            byte[] bytes = this.quiver.fetch(miss.getWindow(), miss.getPacket());
            header.setId(miss.getPacket());
            header.setWindow(miss.getWindow());
            header.setLength(bytes.length);
            header.setSize(this.quiver.getFileSize());
            header.setLow(getLow());
            header.setHigh(getHigh());
            header.setType(PacketType.Data.ordinal());
            byte[] headData = header.data();
            byte[] packetData = new byte[headData.length + bytes.length];
            System.arraycopy(headData, 0, packetData, 0, headData.length);
            System.arraycopy(bytes, 0, packetData, headData.length, bytes.length);
            bow.send(packetData);
        } catch (IOException ex) {
            Logger.getLogger(Archer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void shoot() {
        try {
            header.setId(this.quiver.getPacketId());
            header.setWindow(this.quiver.getWindowId());
            header.setScore(this.quiver.getPacketId());
            byte[] bytes = this.quiver.fetch();
            header.setSize(this.quiver.getFileSize());
            header.setLength(bytes.length);
            header.setLow(getLow());
            header.setHigh(getHigh());
            header.setType(PacketType.Data.ordinal());

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
     * @return the createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * @return the status
     */
    public ArcherStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ArcherStatus status) {
        this.status = status;
    }

    /**
     * @return the bandwidth
     */
    public long getBandwidth() {
        return bandwidth;
    }

    /**
     * @param bandwidth the bandwidth to set
     */
    public void setBandwidth(long bandwidth) {
        if (bandwidth > getLimitBandwidth()) {
            bandwidth = getLimitBandwidth();
        }
        if (bandwidth > this.getLastMaxBandwidth()) {
            this.lastMaxBandwidth = bandwidth;
        }
        this.bandwidth = bandwidth;
    }

    /**
     * @return the lastMaxBandwidth
     */
    public long getLastMaxBandwidth() {
        return lastMaxBandwidth;
    }

    /**
     * @return the limitBandwidth
     */
    public long getLimitBandwidth() {
        return limitBandwidth;
    }

    /**
     * @param limitBandwidth the limitBandwidth to set
     */
    public void setLimitBandwidth(long limitBandwidth) {
        this.limitBandwidth = limitBandwidth;
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

}
