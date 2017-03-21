
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
    
    /* getSocket()
     *    Creates a socket and input/output streams for TCP communication.
     */
    public Socket getSocket(ServerInfo info) throws IOException {
        Socket server = new Socket(info.getIpAddress(), info.getPortNumber());
        din.set(new Scanner(server.getInputStream()));
        psOut.set(new PrintStream(server.getOutputStream()));
        return server;
        //otherServer.set(server);
    }
    
    protected abstract void processInput();
    
    protected abstract void send(String message);
    
    @Override
    public void run(){
        processInput();
    }
}
