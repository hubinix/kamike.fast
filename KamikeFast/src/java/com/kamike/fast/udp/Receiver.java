/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast.udp;

import com.kamike.fast.FastConfig;
import com.kamike.fast.FastInst;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class Receiver implements Runnable {

    DatagramSocket socket;
    int port;
    FastInst inst;
    byte[] buffer;

    private Header header;
    private Result result;
    private double lost;

    public Receiver(FastInst inst, int port) {
        this.inst = inst;
        this.port = port;
        this.buffer = new byte[FastConfig.DataLength];
        this.header = new Header();
        this.result = new Result();
    }

    @Override
    public void run() {
        while (inst.Listen) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                //解析头部
                this.header.load(packet);

                //是否是数据的
                PacketType type = PacketType.values()[this.header.getType()];

                switch (type) {
                    //为了发送，发现丢包，调整速度
                    case Target:

                        Archer archer = inst.getArcher(header.getHigh(), header.getLow());
                        if (archer == null) {
                            return;
                        }
                        archer.update();//有回包，说明网络OK
                        int packetSend = archer.getPacket(header.getWindow());
                        lost = (packetSend - header.getScore()) / packetSend;
                        //archer.setBandwidth((long)(archer.getBandwidth()/lost));
                        if (lost > 0.10) {
                            archer.setBandwidth((long) (archer.getBandwidth() / lost));
                        } else {
                            if (lost < 0.01) {
                                archer.setBandwidth((long) (archer.getBandwidth() * 1.2));
                            } else {
                                //啥也不做保持原速
                            }
                        }
                        //补射
                        this.result.load(header, packet);
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
                    //
                    case Data:
                        Target target = inst.getTarget(header.getHigh(), header.getLow());
                        //新的文件传递
                        if (target == null) {
                            Bow initBow = new Bow(packet.getAddress(), packet.getPort());
                            target = new Target(initBow, header.getHigh(), header.getLow());
                            target.open();
                            inst.addTarget(target);//这里启动的线程，此线程只是用来测量心跳
                        }
                        //是否需要换弓,应对对称式的nat,必要的情况可以缓冲多把弓，目前只有一把
                        Bow bow = target.getBow();
                        if (bow != null) {
                            if ((!bow.getAddress().equals(packet.getAddress())) && bow.getPort() != packet.getPort()) {
                                bow.close();
                                Bow initBow = new Bow(packet.getAddress(), packet.getPort());
                                target.setBow(initBow);
                            }
                        }
                        Iterator<Window> iter = target.Windows(); //此处没有限制window的数量，有可能导致系统崩溃，不过测试应该问题不大
                        boolean isExist = false;
                        Window win = null;
                        while (iter.hasNext()) {
                            win = iter.next();
                            if (win.getId() == header.getWindow()) {

                                isExist = true;
                                break;
                            }
                        }
                        if (win == null || !isExist) {
                            //此报文对应的窗口不存在
                            if (header.getWindow() > target.getPosition() / FastConfig.WindowLength) {
                                win = new Window(header);
                                this.result.load(header, packet);
                                win.setData(header.getId(), this.result.getBuffer());
                                target.addWindow(win);
                            } else {
                                //如果是陈旧报文，则丢弃此报文
                            }
                        } else {
                            this.result.load(header, packet);
                            win.setData(header.getId(), this.result.getBuffer());
                            if (win.isFull()) {
                                target.write(header.getWindow(), buffer);
                                //删除此窗口
                                target.removeWindow(win);
                                
                                
                            }
                        }

                        break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
