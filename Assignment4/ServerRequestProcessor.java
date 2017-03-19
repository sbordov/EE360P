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
    
    public void processInput(String[] input){
        
        String[] clockTokens = input[1].split(":");
        hisId = Integer.parseInt(clockTokens[0]);
        hisTs = Integer.parseInt(clockTokens[1]);
        requestTokens.set(input[2].split("\\s"));
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
