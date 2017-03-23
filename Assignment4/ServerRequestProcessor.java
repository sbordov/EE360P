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
            onReceiveAck(input);
        } else if(input[1].equals("REQUEST")){
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
    
    public void onReceiveAck(String[] input){
        int serverId = Integer.parseInt(input[2]);
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        int id = Integer.parseInt(input[4]);
        boolean enoughAcks = myServer.incrementNumAcks(id);
        boolean isSmallestProcessInQueue = myServer.isProcessAtFrontOfQueue(id);
        if(enoughAcks && isSmallestProcessInQueue){
            myServer.performTransaction();
            sendRelease(id);
        }
        processNextTransactionIfSameServerId();
    }
    
    public void onReceiveRelease(String[] input){
        int time = Integer.parseInt(input[3]);
        myServer.clock.receiveAction(time);
        // Don't need to send message to client.
        myServer.performTransaction();
        processNextTransactionIfSameServerId();
    }
    
     public void processNextTransactionIfSameServerId(){
        ServerUpdateRequest nextProcess;
        int processId;
        boolean enoughAcks = false;
        boolean isSmallestProcessInQueue = false;
        do{
            nextProcess = myServer.pendingQ.peek();
            if(nextProcess == null){
                return;
            }
            processId = nextProcess.processId;
            enoughAcks = myServer.checkNumAcks(nextProcess.processId);
            isSmallestProcessInQueue =
                    myServer.isProcessAtFrontOfQueue(nextProcess.processId);
            if(enoughAcks && isSmallestProcessInQueue){
                // Don't need to send message to client.
                myServer.performTransaction();
                sendRelease(processId);
            }
        } while(enoughAcks && isSmallestProcessInQueue);
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
    
    // RELEASE Message has the following format:
    //  "SERVER_CONNECTION;RELEASE;<Server_Id>;<Time_Stamp>;<Released_Process_Id>"
    public void sendRelease(int id){
        StringBuilder releaseMessage = new StringBuilder();
        // "SERVER_CONNECTION;"
        releaseMessage.append(Symbols.serverMessageHeader);
        // "RELEASE;"
        releaseMessage.append(Symbols.releaseMessageTag);
        // "<Server_Id>;"
        releaseMessage.append(Integer.toString(myServer.myId)).append(";");
        // "<Time_Stamp>;"
        releaseMessage.append(Integer.toString(myServer.clock.sendAction())).append(";");
        // "<RELEASEd_Process_Id>"
        releaseMessage.append(Integer.toString(id));
        Socket s = (Socket) otherServer.get();
        try {
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendToAll(releaseMessage.toString());
        
    }
    
    public void sendToAll(String message){
        for(Integer serverId : myServer.serverList.keySet()){
            if(serverId != myServer.myId){
                try {
                    Socket s = getSocket(myServer.serverList.get(serverId));
                    PrintStream printStreamOut = (PrintStream) psOut.get();
                    printStreamOut.println(message);
                    printStreamOut.flush();
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void send(String message){
        try {
            Socket s = (Socket) otherServer.get();
            PrintWriter pOut = (PrintWriter) pout.get();
            pOut.print(message);
            pOut.flush();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* run()
     *      Effectively the "On receive" method in Lamport's Mutex algorithm.
     *      
     */
    public void run() {
        processInput();
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
