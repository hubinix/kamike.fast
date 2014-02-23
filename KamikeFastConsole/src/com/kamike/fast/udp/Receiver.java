/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import com.kamike.fast.FastInst;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Receiver implements Runnable {

    private Udp udp;
    int port;
    FastInst inst;
    byte[] buffer;

    private Header header;
    private Result result;
    private double lost;
    private SocketAddress address;

    public Receiver(FastInst inst, int port) {
        this.inst = inst;
        this.port = port;
        this.buffer = new byte[FastConfig.DataLength];
        this.header = new Header();
        this.result = new Result();
        this.udp = new Udp(port);
    }

    //接收到下载信号，开始传输数据
    public void beginDownload(byte[] data) {
        Upload upload = inst.getUpload(header.getHigh(), header.getLow());
        this.result.load(header, data);
        if (upload == null) {
            String fileName = new String(this.result.getBuffer(), 0, this.header.getLength());
            Quiver quiver = new Quiver(fileName);
            boolean ret = quiver.open();
            if (!ret) {
                return;
            }
            Udp newUdp = new Udp();
            newUdp.setAddress(address);
            upload = new Upload(newUdp, quiver);
            inst.start(upload);

        }
        upload.update();

    }

    public void report(Window window) {
        try {
            byte[] bytes = window.getHits();
            header.setId(0);
            header.setWindow(window.getId());
            header.setLength(bytes.length);
            header.setSize(0);
            header.setLow(window.getLow());
            header.setHigh(window.getHigh());
            header.setScore(window.getHitCount());
            header.setType(PacketType.UploadStatus.ordinal());
            byte[] headData = header.data();
            byte[] packetData = new byte[headData.length + bytes.length];
            System.arraycopy(headData, 0, packetData, 0, headData.length);
            System.arraycopy(bytes, 0, packetData, headData.length, bytes.length);
            this.udp.send(packetData, address);
        } catch (IOException ex) {
            Logger.getLogger(Upload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reportFinish() {
        try {

            header.setId(0);
            header.setWindow(0);
            header.setLength(0);
            header.setSize(0);
            header.setLow(header.getLow());
            header.setHigh(header.getHigh());
            header.setScore(0);
            header.setType(PacketType.Finish.ordinal());
            byte[] headData = header.data();

            this.udp.send(headData, address);
        } catch (IOException ex) {
            Logger.getLogger(Upload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //接收到上传信号，开始准备接收数据，通知远方开始上传，发送下载信号
    public void beginUpload(byte[] data) {
        Target target = inst.getTarget(header.getHigh(), header.getLow());
        this.result.load(header, data);
        if (target == null) {
            String fileName = new String(this.result.getBuffer(), 0, header.getLength());
            target = new Target(header.getHigh(), header.getLow(), fileName);
            target.open();
            inst.addTarget(target);
            //下面通知开始上传,给远方发送下载信号
            this.notifyStartUpload();
        }
        target.update();

    }

    public void notifyStartUpload() {
        try {

            header.setId(0);
            header.setWindow(0);
            header.setLength(0);
            header.setSize(0);
            header.setLow(header.getLow());
            header.setHigh(header.getHigh());
            header.setScore(0);
            header.setType(PacketType.BeginDownload.ordinal());
            byte[] headData = header.data();
            this.udp.send(headData, address);
        } catch (IOException ex) {
            Logger.getLogger(Upload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //接收数据
    public void receiving(byte[] data) {
        Target target = inst.getTarget(header.getHigh(), header.getLow());
        if (target == null) {
            return;
        }
        if (target.getReceivingWindow() == null) {
            Window win = new Window(header);
            this.result.load(header, data);
            win.setData(header.getId(), this.result.getBuffer());
            target.setReceivingWindow(win);
        }
        if (target.getReceivingWindow().getId() <= header.getWindow()) {
            this.result.load(header, data);
            target.getReceivingWindow().setData(header.getId(), this.result.getBuffer());
            if (target.getReceivingWindow().isFull()) {

                target.write(target.getReceivingWindow().getId(), target.getReceivingWindow().getBuffer());

                this.report(target.getReceivingWindow());
                target.setConfirmingWindow(target.getReceivingWindow());
                target.setReceivingWindow(null);

            }
        } else {

            target.setConfirmingWindow(target.getReceivingWindow());
            target.setReceivingWindow(null);
        }

    }

    public void retryData(byte[] data) {
        Target target = inst.getTarget(header.getHigh(), header.getLow());
        if (target == null) {
            return;
        }
        if (target.getConfirmingWindow() == null) {
            Window win = new Window(header);
            this.result.load(header, data);
            win.setData(header.getId(), this.result.getBuffer());
            target.setConfirmingWindow(win);
        }
        if (target.getReceivingWindow().getId() == header.getLastWindow()) {
            if (target.getReceivingWindow().getId() == header.getWindow()) {
                this.result.load(header, data);
                target.getReceivingWindow().setData(header.getId(), this.result.getBuffer());
                if (target.getReceivingWindow().isFull()) {
                    //正好是下一个窗口的报文
                    target.write(target.getReceivingWindow().getId(), target.getReceivingWindow().getBuffer());
                    //删除此窗口
                    this.report(target.getReceivingWindow());
                }
            }
        }
        if (target.getConfirmingWindow().getId() == header.getWindow()) {
            this.result.load(header, data);
            target.getConfirmingWindow().setData(header.getId(), this.result.getBuffer());
            if (target.getConfirmingWindow().isFull()) {
                //正好是下一个窗口的报文
                target.write(target.getConfirmingWindow().getId(), target.getConfirmingWindow().getBuffer());
                //删除此窗口
                this.report(target.getConfirmingWindow());
                target.setConfirmingWindow(null);
            }
        }

    }

    public void confirm() {
        Target target = inst.getTarget(header.getHigh(), header.getLow());
        if (target == null) {
            return;
        }
        if (target.getConfirmingWindow() == null) {
            Window win = new Window(header);
            target.setConfirmingWindow(win);
        }
        this.report(target.getConfirmingWindow());
    }

    public void finish() {
        Target target = inst.getTarget(header.getHigh(), header.getLow());
        if (target == null) {
            return;
        }
        if (target.getConfirmingWindow() == null) {
            Window win = new Window(header);
            target.setConfirmingWindow(win);
        }
        if (target.getConfirmingWindow().isFull()) {
            if (target.getReceivingWindow().isFull()) {
                this.reportFinish();
                target.close();
            } else {
                this.report(target.getReceivingWindow());
            }
        } else {
            this.report(target.getConfirmingWindow());
        }

    }

    @Override
    public void run() {
        while (inst.Listen) {
            byte[] packet = udp.recv();
            address = udp.getAddress();
            this.header.load(packet);
            PacketType type = PacketType.values()[this.header.getType()];
            switch (type) {
                case UploadStatus:
                    this.confirm();
                    break;
                case Data:
                    this.receiving(packet);
                    break;
                case RetryData:
                    this.retryData(packet);
                    break;
                case BeginUpload:
                    this.beginUpload(packet);
                    break;
                case BeginDownload:
                    this.beginDownload(packet);
                    break;
                case Finish:
                    this.finish();
                    break;

            }
        }
    }

}
