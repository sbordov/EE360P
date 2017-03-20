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
        if(input[1].equals("ACK")){
            messageType.set(MessageType.ACK);
            onReceiveAck(input);
        } else if(input[1].equals("REQUEST")){
            messageType.set(MessageType.REQUEST);
            onReceiveRequest(input);
        } else{
            throw new UnsupportedOperationException("Received neither ACK nor REQUEST");
        }
    }
    
    public void onReceiveAck(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        int id = Integer.parseInt(input[4]);
        
        
    }
    
    public void onReceiveRequest(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        int id = Integer.parseInt(input[4]);
        String[] reqTokens = input[5].split("\\s");
        ServerUpdateRequest request = new ServerUpdateRequest(time, id, reqTokens);
        myServer.insertToPendingQueue(request);
        sendAck(id);
    }
    
    public void sendAck(int id){
        StringBuilder ackMessage = new StringBuilder();
        ackMessage.append(Symbols.serverMessageHeader).append(Symbols.ackMessageTag);
        ackMessage.append(Integer.toString(myServer.clock.sendAction()));
        ackMessage.append(Integer.toString(id));
    }
    
    /* run()
     *      Effectively the "On receive" method in Lamport's Mutex algorithm.
     *      
     */
    public void run() {
        try {
            StringBuilder response = new StringBuilder();
            response.append(Symbols.serverMessageHeader);
            // Send appropriate command to the server and display the
                // appropriate responses from the server
            PrintWriter pOut = (PrintWriter) pout.get();
            Socket s = (Socket) otherServer.get();
            pOut.print(response.toString());
            pOut.flush();
            s.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
