/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
import java.net.*; import java.io.*; import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class ServerRequestProcessor extends RequestProcessor implements Runnable {
    /*
    private static LamportClock clock;
    private static PriorityQueue<ClientRequest> pendingQ = new PriorityQueue<>();
    private Inventory inventory;
    */

    
    public ServerRequestProcessor(Socket s, String[] input,
            Server server) {
        super(s, input, server);
    }
    
    public void processInput(){
        String[] input = this.getInputTokens();
        if(input[1].equals(Symbols.requestMessageTag)){
            onReceiveRequest(input);
        } else if(input[1].equals(Symbols.releaseMessageTag)){
            onReceiveRelease(input);
        } else if(input[1].equals(Symbols.ackMessageTag)){
            onReceiveAck(input);
        } else if(input[1].equals(Symbols.imAliveMessageTag)){
            onReceiveImAlive(input);
        } else if(input[1].equals(Symbols.rUAliveMessageTag)){
            onReceiveRUAlive(input);
        } else{
            throw new UnsupportedOperationException("Received neither ACK nor REQUEST");
        }
    }
    
    // TODO
    public void onReceiveImAlive(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
    }
    
    public void onReceiveRUAlive(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        sendImAlive(serverId);
    }
    
    public void onReceiveRequest(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        int id = Integer.parseInt(input[4]);
        System.out.println(input);
        String[] reqTokens = input[5].split("\\s");
        ServerUpdateRequest request = new ServerUpdateRequest(serverId, time, id, reqTokens);
        myServer.insertToPendingQueue(request);
        sendAck(id, serverId);
    }
    
    public void onReceiveRelease(String[] input){
        int id = Integer.parseInt(input[4]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        // Don't need to send message to client.
        myServer.performTransaction();
        myServer.pendingQ.poll();
        processNextTransactionIfSameServerId();
    }
    
     
    public void onReceiveAck(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        int id = Integer.parseInt(input[4]);
        boolean enoughAcks = myServer.incrementNumAcks(id);
        boolean isSmallestProcessInQueue = myServer.isProcessAtFrontOfQueue(id);
        if(enoughAcks && isSmallestProcessInQueue){
            release(id);
        }
        processNextTransactionIfSameServerId();
    }
    
    public void sendRUAlive(){
        StringBuilder rUAliveMessage = new StringBuilder();
        // "SERVER_CONNECTION;"
        rUAliveMessage.append(Symbols.serverMessageHeader);
        rUAliveMessage.append(Symbols.messageDelimiter);
        // "RUALIVE;"
        rUAliveMessage.append(Symbols.rUAliveMessageTag);
        rUAliveMessage.append(Symbols.messageDelimiter);
        // "<Server_Id>"
        rUAliveMessage.append(Integer.toString(myServer.myId));
        rUAliveMessage.append(Symbols.messageDelimiter);
        // "<Time_Stamp>;"
        rUAliveMessage.append(Integer.toString(myServer.clock.sendAction()));
        rUAliveMessage.append(Symbols.messageDelimiter);
        sendToAll(rUAliveMessage.toString());
    }
    
    public void sendImAlive(int serverId){
        StringBuilder imAliveMessage = new StringBuilder();
        // "SERVER_CONNECTION;"
        imAliveMessage.append(Symbols.serverMessageHeader);
        imAliveMessage.append(Symbols.messageDelimiter);
        // "IMALIVE;"
        imAliveMessage.append(Symbols.imAliveMessageTag);
        imAliveMessage.append(Symbols.messageDelimiter);
        // "<Server_Id>"
        imAliveMessage.append(Integer.toString(myServer.myId));
        imAliveMessage.append(Symbols.messageDelimiter);
        // "<Time_Stamp>;"
        imAliveMessage.append(Integer.toString(myServer.clock.sendAction()));
        imAliveMessage.append(Symbols.messageDelimiter);
        try {
            Socket s = this.otherServer;
            //PrintWriter psOut = this.getPout();
            psOut.print(imAliveMessage.toString());
            psOut.flush();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // ACK Message has the following format:
    //  "SERVER_CONNECTION;ACK;<Server_Id>;<Time_Stamp>;<ACKed_Process_Id>"
    public void sendAck(int id, int serverId){
        StringBuilder ackMessage = new StringBuilder();
        // "SERVER_CONNECTION;"
        ackMessage.append(Symbols.serverMessageHeader);
        ackMessage.append(Symbols.messageDelimiter);
        // "ACK;"
        ackMessage.append(Symbols.ackMessageTag);
        ackMessage.append(Symbols.messageDelimiter);
        // "<Server_Id>;"
        ackMessage.append(Integer.toString(myServer.myId));
        ackMessage.append(Symbols.messageDelimiter);
        // "<Time_Stamp>;"
        ackMessage.append(Integer.toString(myServer.clock.sendAction()));
        ackMessage.append(Symbols.messageDelimiter);
        // "<ACKed_Process_Id>"
        ackMessage.append(Integer.toString(id));
        try {
            Socket s = getSocket(myServer.getServerList().get(serverId));
            PrintWriter pOut = new PrintWriter(s.getOutputStream());
            pOut.print(ackMessage.toString());
            pOut.flush();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        send(ackMessage.toString());
    }
    
}
