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
public class ClientRequestProcessor extends RequestProcessor implements Runnable {

    public ClientRequestProcessor(Socket s, String[] tokens, Server server) {
        super(s, tokens, server);
    }

    //TODO
    public void processInput(){
        String[] input = this.getInputTokens();
        int serverId = myServer.myId;
        int time = myServer.clock.sendAction();
        int processId = myServer.getAndIncrementNextNewProcessId();
        ServerUpdateRequest request = new ServerUpdateRequest(serverId, time,
                processId, input);
        myServer.insertToPendingQueue(request);
        myServer.insertToMyProcesses(request);
        Socket s = this.getOtherServer();
        if(s == null){
            System.out.println("Damn");
        }
        myServer.insertToClients(processId, s);
        sendRequest(processId);
    }

    public synchronized void sendRequestToAll(String message){
        for(Integer serverId : myServer.serverList.keySet()){
            if(serverId != myServer.myId){
                try {
                    Socket s = getSocket(myServer.serverList.get(serverId));
                    s.setSoTimeout(Symbols.TIMEOUT_DURATION);
                    PrintStream printStreamOut = this.getPsOut();
                    printStreamOut.println(message);
                    printStreamOut.flush();
                    String response;
                    Scanner dataIn = this.getDin();
                    // Read response from ServerSocket.
                    while(dataIn.hasNextLine()){
                        response = dataIn.nextLine();
                        onReceiveAck(response.split(";"));
                    }
                    s.close();
                } catch (IOException ex) {
                    if(ex instanceof java.net.SocketTimeoutException){
                        myServer.addBrokenServerToList(serverId);
                    } else{
                        System.out.println("Not exactly what I wanted");
                        Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        for(int id: myServer.brokenServerIds.keySet()){
            myServer.removeServerFromList(id);
        }
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

    public void sendRequest(int id){
        StringBuilder requestMessage = new StringBuilder();
        // "SERVER_CONNECTION;"
        requestMessage.append(Symbols.serverMessageHeader);
        requestMessage.append(Symbols.messageDelimiter);
        // "REQUEST"
        requestMessage.append(Symbols.requestMessageTag);
        requestMessage.append(Symbols.messageDelimiter);
        // "<Server_Id>;"
        requestMessage.append(Integer.toString(myServer.myId));
        requestMessage.append(Symbols.messageDelimiter);
        // "<Time_Stamp>;"
        requestMessage.append(Integer.toString(myServer.clock.sendAction()));
        requestMessage.append(Symbols.messageDelimiter);
        // "<RELEASEd_Process_Id>;"
        requestMessage.append(Integer.toString(id));
        requestMessage.append(Symbols.messageDelimiter);
        // "<Requested process>"
        requestMessage.append(parcelRequest(id));
        Socket s = this.getOtherServer();
        try {
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        sendRequestToAll(requestMessage.toString());

    }
    
    public String parcelRequest(int processId){
        String[] tokens = this.getInputTokens();
    }

}

