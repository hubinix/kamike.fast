/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Reporter implements Runnable {

    DatagramSocket socket;

    Upload archer;
    byte[] buffer;

    private Header header;
    private Result result;
    private double lost;

    public Reporter(Upload archer) {
        this.archer = archer;

        this.buffer = new byte[FastConfig.DataLength];
        this.header = new Header();
        this.result = new Result();

    }

    @Override
    public void run() {
        while (!archer.isFinish()) {

            try {

                byte[] packetBytes = this.archer.getUdp().recv();
                //解析头部
                this.header.load(packetBytes);

                //是否是数据的
                PacketType type = PacketType.values()[this.header.getType()];

                switch (type) {
                    //为了发送，发现丢包，调整速度
                    case UploadStatus:
                        archer.update();//有回包，说明网络OK

                        if (header.getScore() >= FastConfig.PacketInWindow
                                || (header.getWindow() == this.archer.getQuiver().getLastWindow()
                                && header.getScore() >= this.archer.getQuiver().getLastPacket() + 1)) {
                            this.archer.getQuiver().setConfirmedWindow(header.getWindow());

                            continue;
                        }

                        int packetSend = archer.getPacket(header.getWindow());
                        lost = (double) (packetSend - header.getScore()) / packetSend;
                        //archer.setBandwidth((long)(archer.getBandwidth()/lost));
                        if (lost > 0.10 && lost < 1.0) {
                            archer.setBandwidth((long) (archer.getBandwidth() * (1 - lost)));
                        } else {
                            if (lost < 0.01) {
                                archer.setBandwidth((long) (archer.getBandwidth() * 1.2));
                            } else {
                                //啥也不做保持原速
                            }
                        }
                        //补射

                        this.result.load(header, packetBytes);
                        //archer.cleanMiss();
                        for (int i = 0; i < packetSend; i++) {
                            byte[] data = result.getBuffer();
                            if (data[i] == 0x0) {
                                Miss miss = new Miss();
                                miss.setPacket(i);
                                miss.setWindow(header.getWindow());
                                archer.miss(miss);
                            }
                        }

                        break;
                    case Finish:

                        archer.update();//有回包，说明网络OK
                        this.archer.setFinish(true);

                        break;

                }
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Logger.getLogger(Reporter.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
