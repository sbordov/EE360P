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
    private ThreadLocal<Integer> numAcks = new ThreadLocal<>();
    private ThreadLocal<Integer> hisTs = new ThreadLocal<>();
    private ThreadLocal<Integer> hisId = new ThreadLocal<>();
    private ThreadLocal<MessageType> messageType = new ThreadLocal<>();

    
    public ServerRequestProcessor(Socket s, String[] input,
            Server server) {
        super(s, input, server);
        numAcks.set(0);
        
    }
    
    public void processInput(){
        String[] input = (String[]) inputTokens.get();
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
        String[] reqTokens = input[5].split("\\s");
        ServerUpdateRequest request = new ServerUpdateRequest(serverId, time, id, reqTokens);
        myServer.insertToPendingQueue(request);
        sendAck(id);
    }
    
    public void onReceiveRelease(String[] input){
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        // Don't need to send message to client.
        myServer.performTransaction();
        processNextTransactionIfSameServerId();
    }
    
    // ACK Message has the following format:
    //  "SERVER_CONNECTION;ACK;<Server_Id>;<Time_Stamp>;<ACKed_Process_Id>"
    public void sendAck(int id){
        StringBuilder ackMessage = new StringBuilder();
        // "SERVER_CONNECTION;"
        ackMessage.append(Symbols.serverMessageHeader);
        // "ACK;"
        ackMessage.append(Symbols.ackMessageTag);
        // "<Server_Id>;"
        ackMessage.append(Integer.toString(myServer.myId)).append(";");
        // "<Time_Stamp>;"
        ackMessage.append(Integer.toString(myServer.clock.sendAction())).append(";");
        // "<ACKed_Process_Id>"
        ackMessage.append(Integer.toString(id));
        send(ackMessage.toString());
    }
    
    @Override
    protected void destroyThreadLocals(){
        super.destroyThreadLocals();
        numAcks.remove();
        hisTs.remove();
        hisId.remove();
        messageType.remove();
    }
}
