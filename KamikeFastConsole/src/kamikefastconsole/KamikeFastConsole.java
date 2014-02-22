/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kamikefastconsole;

import com.kamike.fast.FastInst;
import com.kamike.fast.udp.Upload;
import com.kamike.fast.udp.Udp;
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
            
            InetAddress address = InetAddress.getLocalHost();
            int port = 3000;
            Udp bow = new Udp(address, 3000,30000);
            String name = "d:\\data\\LOVEYOU.mp4";
            Quiver quiver = new Quiver(name);
            quiver.open();
            Upload archer = new Upload(bow, quiver);
            FastInst.getInstance().start(archer);
            while (FastInst.getInstance().Listen) {
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
