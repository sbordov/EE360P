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
    public int processId;
    public int logicalClock;
    public String[] processTokens;
    
    public ServerUpdateRequest(int id, int clock, String[] tokens){
        processId = id;
        logicalClock = clock;
        processTokens = tokens;
    }
}
