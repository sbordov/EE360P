
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
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
public abstract class RequestProcessor implements Runnable{
    protected ThreadLocal<Socket> otherServer;
    protected ThreadLocal<PrintWriter> pout;
    protected ThreadLocal<PrintStream> psOut;
    protected ThreadLocal<Scanner> din;
    protected ThreadLocal<String[]> inputTokens;
    protected ThreadLocal<String[]> requestTokens;
    protected Server myServer;
    
    protected enum MessageType{
        REQUEST,
        ACK
    };
    
    public RequestProcessor(Socket s, String[] input,
            Server server) {
        otherServer.set(s);
        try {
            pout.set(new PrintWriter(s.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        inputTokens.set(input);
        myServer = server;
        /*
        clock = c;
        pendingQ = q;
        inventory = inv;
        */
    }
    
    protected abstract void processInput();
    
    @Override
    public void run(){
        processInput();
    }
    
    /* getSocket()
     *    Creates a socket and input/output streams for TCP communication.
     */
    public Socket getSocket(ServerInfo info) throws IOException {
        Socket socket = new Socket(info.getIpAddress(), info.getPortNumber());
        din.set(new Scanner(socket.getInputStream()));
        psOut.set(new PrintStream(socket.getOutputStream()));
        return socket;
        //otherServer.set(server);
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
    
    protected void destroyThreadLocals(){
        otherServer.remove();
        pout.remove();
        psOut.remove();
        din.remove();
        inputTokens.remove();
        requestTokens.remove();
    }
}
