/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kamike.fast;

import com.kamike.fast.udp.Archer;
import com.kamike.fast.udp.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author THiNk
 */
public class FastInst {

    private static FastInst inst = new FastInst();
    private List<Archer> archers;
    private List<Target> targets;
    public volatile boolean Listen = false;

    private FastInst() {
        archers = Collections.synchronizedList(new ArrayList<Archer>());
        targets = Collections.synchronizedList(new ArrayList<Target>());
        this.Listen = true;

    }

    public void start(Archer archer) {
        this.archers.add(archer);
    }

    public void refresh() {
        for (Archer archer : archers) {
            archer.update();
        }
    }

    public Archer getArcher(long high, long low) {
        for (Archer archer : archers) {
            if (archer.getHigh() == high && archer.getLow() == low) {
                return archer;
            }
        }
        return null;
    }

    public Target getTarget(long high, long low) {
        for (Target target : targets) {
            if (target.getHigh() == high && target.getLow() == low) {
                return target;
            }
        }

        return null;
    }

    public void addTarget(Target target) {
        for (Target t : targets) {
            if (t.getHigh() == target.getHigh() && t.getLow() == target.getLow()) {
                return;
            }
        }
        targets.add(target);
        new Thread(target).start();
    }

    public static FastInst getInstance() {
        return inst;
    }

}
