
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
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
public abstract class RequestProcessor {
    protected ThreadLocal<Socket> otherServer;
    protected ThreadLocal<PrintWriter> pout;
    protected ThreadLocal<String[]> requestTokens;
    protected Server myServer;
    
    public RequestProcessor(Socket s, String[] input,
            Server server) {
        otherServer.set(s);
        try {
            pout.set(new PrintWriter(s.getOutputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        processInput(input);
        myServer = server;
        /*
        clock = c;
        pendingQ = q;
        inventory = inv;
        */
    }
    
    protected abstract void processInput(String[] input);
}
