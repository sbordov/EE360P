
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class ServerTimeoutThread implements Runnable{
    int myServerId;
    int myProcessId;
    Server myServer;
    
    public ServerTimeoutThread(int processId, int serverId){
        myServerId = serverId;
        myProcessId = processId;
    }
    
    @Override
    public void run(){
        try {
            Thread.sleep(100);
            myServer.addBrokenServerToList(myServerId);
            myServer.removeBrokenServerProcesses(myServerId);
            myServer.notifyNextThreadInQueue(myProcessId);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerTimeoutThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
