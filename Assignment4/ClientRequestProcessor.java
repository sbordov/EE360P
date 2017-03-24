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
        String[] tokens = input[1].split("\\s");
        this.setRequestTokens(tokens);
        int serverId = myServer.myId;
        int time = myServer.clock.sendAction();
        int processId = myServer.getAndIncrementNextNewProcessId();
        ServerUpdateRequest request = new ServerUpdateRequest(serverId, time,
                processId, tokens);
        myServer.insertToPendingQueue(request);
        myServer.insertToMyProcesses(request);
        Socket s = this.getOtherServer();
        if(s == null){
        }
        myServer.insertToClients(processId, s);
        myServer.processTimebombs.put(processId, new HashMap<Integer, Thread>());
        sendRequest(processId);
    }

    public synchronized void sendRequestToAll(String message, int processId){
        
        for(Integer serverId : myServer.serverList.keySet()){
            if(serverId != myServer.myId){
                try {
                    Socket s = getSocket(myServer.serverList.get(serverId));
                    //s.setSoTimeout(Symbols.TIMEOUT_DURATION);
                    PrintStream printStreamOut = this.getPsOut();
                    Thread bomb = new Thread(new ServerTimeoutThread(processId, myServer.myId));
                    bomb.start();
                    myServer.processTimebombs.get(processId).put(serverId, bomb);
                    printStreamOut.println(message);
                    printStreamOut.flush();
                    /*
                    String response;
                    Scanner dataIn = this.getDin();
                    // Read response from ServerSocket.
                    while(dataIn.hasNextLine()){
                        response = dataIn.nextLine();
                        onReceiveAck(response.split(";"));
                    }
                    */
                    s.close();
                } catch (IOException ex) {
                    if(ex instanceof java.net.SocketTimeoutException){
                        myServer.addBrokenServerToList(serverId);
                    } else{
                        Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        for(int id: myServer.brokenServerIds.keySet()){
            myServer.removeServerFromList(id);
        }
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

        sendRequestToAll(requestMessage.toString(), id);

    }
    
    public String parcelRequest(int processId){
        String[] tokens = this.getRequestTokens();
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for(String token : tokens){
            sb.append(prefix).append(token);
            if(prefix.equals("")){
                prefix = " ";
            }
        }
        return sb.toString();
    }

}

