/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Quiver {

    public Quiver(String fileName) {
        this.fileName = fileName;
        this.offset = 0;
        this.byteData = new byte[FastConfig.PacketLength];

    }
    private ConcurrentHashMap<Long, byte[]> hashMap = new ConcurrentHashMap<Long, byte[]>();

    public boolean open() {
        try {
            setFile(new RandomAccessFile(getFileName(), "r"));
            this.setFileSize(getFile().length());
            this.lastWindow = (this.getFileSize() - this.getFileSize() % FastConfig.WindowLength) / FastConfig.WindowLength;
            long lastWindowLength = (this.getFileSize() % FastConfig.WindowLength);
            this.lastPacket = (lastWindowLength - lastWindowLength % FastConfig.PacketLength) / FastConfig.PacketLength;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Quiver.class.getName()).log(Level.SEVERE, null, ex);
            setFile(null);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Quiver.class.getName()).log(Level.SEVERE, null, ex);
            setFile(null);
            return false;
        }
        return true;
    }
    private String fileName;

    private long fileSize;
    private RandomAccessFile file;
    private long offset;
    private byte[] buffer;

    private long lastWindow;
    private long lastPacket;

    private byte[] byteData;

    private volatile boolean finish;
    private volatile boolean change;

    private long windowId;
    private int packetId;
    
    public byte[] fetch() {
        setFinish(false);
        this.change=false;
        byte[] result = fetch(getWindowId(), getPacketId());
        if (getWindowId() == lastWindow && getPacketId() == this.lastPacket) {
            setFinish(true);
            return result;
        }
        if ( getPacketId() < FastConfig.PacketInWindow - 1) {
            setPacketId(getPacketId() + 1);
            return result;
        }
        if (getWindowId() < lastWindow && getPacketId() == FastConfig.PacketInWindow - 1) {
            setWindowId(getWindowId() + 1);
            setPacketId(0);
            this.change=true;
        }

        return result;

    }

    public byte[] fetch(long windowId, int packetId) {

        try {
            int packetOffset = packetId * FastConfig.PacketLength;

            if (getFile() == null) {
                return null;
            }

            if (windowId > lastWindow || windowId < 0 || packetId < 0 || packetId > FastConfig.PacketInWindow) {
                return null;
            }
            if (windowId == lastWindow && packetId > this.lastPacket) {
                return null;
            }

            setOffset(windowId * FastConfig.WindowLength);
            setBuffer(new byte[FastConfig.WindowLength]);

            if (getOffset() + FastConfig.WindowLength > this.getFileSize()) {

                int lastLength = (int) (this.getFileSize() - getOffset());
                setBuffer(new byte[lastLength]);

            }
            if (getHashMap().containsKey(windowId)) {
                setBuffer(getHashMap().get(windowId));
            } else {
                getFile().seek(getOffset());
                getFile().read(getBuffer(), 0, getBuffer().length);
                getHashMap().put(windowId, getBuffer());
                if (getHashMap().containsKey(windowId - 2L)) {
                    getHashMap().remove(windowId - 2L);
                }
            }
            if (packetOffset + FastConfig.PacketLength > getBuffer().length) {
                setByteData(new byte[getByteData().length - packetOffset]);

            } else {
                setByteData(new byte[getByteData().length]);
            }
            System.arraycopy(getBuffer(), packetOffset, getByteData(), 0, getByteData().length);

        } catch (IOException ex) {
            Logger.getLogger(Quiver.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return getByteData();

    }

    public void close() {
        try {
            this.getFile().close();
            this.getHashMap().clear();
        } catch (Exception ex) {
            Logger.getLogger(Quiver.class.getName()).log(Level.SEVERE, null, ex);
            this.hashMap = null;
            this.setFile(null);
        }
    }

    /**
     * @return the hashMap
     */
    public ConcurrentHashMap<Long, byte[]> getHashMap() {
        return hashMap;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the file
     */
    public RandomAccessFile getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(RandomAccessFile file) {
        this.file = file;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(long offset) {
        this.offset = offset;
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
        this.buffer=buffer;
    }

    /**
     * @return the lastWindow
     */
    public long getLastWindow() {
        return lastWindow;
    }

    /**
     * @param lastWindow the lastWindow to set
     */
    public void setLastWindow(long lastWindow) {
        this.lastWindow = lastWindow;
    }

    /**
     * @return the byteData
     */
    public byte[] getByteData() {
        return byteData;
    }

    /**
     * @param byteData the byteData to set
     */
    public void setByteData(byte[] byteData) {
       this.byteData=byteData;
    }

   

    /**
     * @return the finish
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * @param finish the finish to set
     */
    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    /**
     * @return the windowId
     */
    public long getWindowId() {
        return windowId;
    }

    /**
     * @param windowId the windowId to set
     */
    public void setWindowId(long windowId) {
        this.windowId = windowId;
    }

    /**
     * @return the packetId
     */
    public int getPacketId() {
        return packetId;
    }

    /**
     * @param packetId the packetId to set
     */
    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    /**
     * @return the change
     */
    public boolean isChange() {
        return change;
    }

    /**
     * @param change the change to set
     */
    public void setChange(boolean change) {
        this.change = change;
    }
}
