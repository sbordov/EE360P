
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
    protected Socket otherServer;
    protected PrintWriter pout;
    protected PrintStream psOut;
    protected Scanner din;
    protected String[] inputTokens;
    protected String[] requestTokens;
    protected Server myServer;

    public RequestProcessor(Socket s, String[] input,
            Server server) {
        this.setOtherServer(s);
        try {
            this.setPout(new PrintWriter(s.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setInputTokens(input);
        this.setMyServer(server);
        /*
        clock = c;
        pendingQ = q;
        inventory = inv;
        */
    }
    
    protected abstract void processInput();
    
    @Override
    public void run(){
        Socket s = this.getOtherServer();
        if(s == null){
        }
        processInput();
    }
    
    /* getSocket()
     *    Creates a socket and input/output streams for TCP communication.
     */
    public Socket getSocket(ServerInfo info) throws IOException {
        Socket socket = new Socket(info.getIpAddress(), info.getPortNumber());
        this.setDin(new Scanner(socket.getInputStream()));
        this.setPsOut(new PrintStream(socket.getOutputStream()));
        return socket;
        //otherServer.set(server);
    }
    
     public void send(String message){
        try {
            Socket s = this.getOtherServer();
            PrintWriter pOut = this.getPout();
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
                    PrintStream printStreamOut = (PrintStream) this.getPsOut();
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
        releaseMessage.append(Symbols.messageDelimiter);
        // "RELEASE;"
        releaseMessage.append(Symbols.releaseMessageTag);
        releaseMessage.append(Symbols.messageDelimiter);
        // "<Server_Id>;"
        releaseMessage.append(Integer.toString(myServer.myId));
        releaseMessage.append(Symbols.messageDelimiter);
        // "<Time_Stamp>;"
        releaseMessage.append(Integer.toString(myServer.clock.sendAction()));
        releaseMessage.append(Symbols.messageDelimiter);
        // "<RELEASEd_Process_Id>"
        releaseMessage.append(Integer.toString(id));
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
                /*
                myServer.performTransaction();
                sendRelease(processId);
                */
                release(processId);
            }
        } while(enoughAcks && isSmallestProcessInQueue);
    }
    
    public void release(int processId){
        String response = myServer.performTransaction();
        sendRelease(processId);
        myServer.respondToClient(processId, response);
    }
    
    public Socket getOtherServer() {
        return otherServer;
    }

    public void setOtherServer(Socket otherServer) {
        this.otherServer = otherServer;
    }

    public PrintWriter getPout() {
        return pout;
    }

    public void setPout(PrintWriter pout) {
        this.pout = pout;
    }

    public PrintStream getPsOut() {
        return psOut;
    }

    public void setPsOut(PrintStream psOut) {
        this.psOut = psOut;
    }

    public Scanner getDin() {
        return din;
    }

    public void setDin(Scanner din) {
        this.din = din;
    }

    public String[] getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(String[] inputTokens) {
        this.inputTokens = inputTokens;
    }

    public String[] getRequestTokens() {
        return requestTokens;
    }

    public void setRequestTokens(String[] requestTokens) {
        this.requestTokens = requestTokens;
    }

    public Server getMyServer() {
        return myServer;
    }

    public void setMyServer(Server myServer) {
        this.myServer = myServer;
    }
    
    protected enum MessageType{
        REQUEST,
        ACK
    };
    
    /*
    protected void destroyThreadLocals(){
        otherServer.remove();
        pout.remove();
        psOut.remove();
        din.remove();
        inputTokens.remove();
        requestTokens.remove();
    }
    */
}
