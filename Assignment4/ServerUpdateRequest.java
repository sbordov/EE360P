
import java.net.Socket;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class ServerUpdateRequest {
    public int serverId;
    public int timeStamp;
    public int processId;
    public String[] processTokens;
    public int numAcks = 0;
    
    public ServerUpdateRequest(int server, int time, int id, String[] tokens){
        serverId = server;
        timeStamp = time;
        processId = id;
        processTokens = tokens;
    }
    
    public synchronized int incrementAndGetNumAcks(){
        return ++numAcks;
    }
    
    public synchronized int getNumAcks(){
        return numAcks;
    }
    
}
