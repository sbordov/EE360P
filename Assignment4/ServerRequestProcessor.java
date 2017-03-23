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
        if(input[1].equals("REQUEST")){
            onReceiveRequest(input);
        } else if(input[1].equals("RELEASE")){
            onReceiveRelease(input);
        } else{
            throw new UnsupportedOperationException("Received neither ACK nor REQUEST");
        }
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
        sendAck(id);
    }
    
    public void onReceiveRelease(String[] input){
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        // Don't need to send message to client.
        System.out.println("Performing transaction.");
        myServer.performTransaction();
        processNextTransactionIfSameServerId();
    }
    
    // ACK Message has the following format:
    //  "SERVER_CONNECTION;ACK;<Server_Id>;<Time_Stamp>;<ACKed_Process_Id>"
    public void sendAck(int id){
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
        send(ackMessage.toString());
    }
    
}
