/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author THiNk
 */
public class Window {
    
    private long id;
    private long low;
    private long high;
    private byte[] buffer;
    private Date createDate;
    private byte[] hits;

    private boolean full;
    private int hitCount;

    private volatile ReentrantLock lock = new ReentrantLock();
    
    @Override
    public boolean equals(Object o)
    {
        if(this.hashCode()==o.hashCode())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 71 * hash + (int) (this.low ^ (this.low >>> 32));
        hash = 71 * hash + (int) (this.high ^ (this.high >>> 32));
        return hash;
    }

   

    public boolean isFull() {
        if (full) {
            return full;
        }
        if (hits == null) {
            return false;
        }
        for (byte hit : hits) {
            if (hit == 0x0) {
                this.full = false;
                return full;
            }
        }
        return full;
    }

    public void setData(int position, byte[] data) {
        lock.lock();
        try {
            if (hits == null) {
                hits = new byte[FastConfig.PacketInWindow];
                setHitCount(0);
            }
            hits[position] = 0x1;
            System.arraycopy(data, 0, buffer, position, data.length);
            setHitCount(getHitCount() + 1);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
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

    /**
     * @return the buffer
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
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
     * @return the hits
     */
    public byte[] getHits() {
        return hits;
    }

    /**
     * @param hits the hits to set
     */
    public void setHits(byte[] hits) {
        this.hits = hits;
    }

    /**
     * @return the hitCount
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * @param hitCount the hitCount to set
     */
    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }
}