/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kamikefastconsole;

import com.kamike.fast.FastInst;
import com.kamike.fast.udp.Archer;
import com.kamike.fast.udp.Bow;
import com.kamike.fast.udp.Quiver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author THiNk
 */
public class KamikeFastConsole {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            InetAddress address=InetAddress.getByName("127.0.0.1");
            int port=3000;
            Bow bow=new Bow(address,3000);
            String name="d:\\data\\LOVEYOU.mp4";
            Quiver quiver=new Quiver(name);
            quiver.open();
            Archer archer=new Archer(bow,quiver);
            FastInst.getInstance().start(archer);
            while(FastInst.getInstance().Listen)
            {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KamikeFastConsole.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(KamikeFastConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
