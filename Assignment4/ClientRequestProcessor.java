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
public class ClientRequestProcessor extends Thread {
    Socket otherServer;
    PrintWriter pout;
    String[] communicationTokens;
    Inventory inventory;
    
    public ClientRequestProcessor(Socket s, PrintWriter pOut, String[] tokens,
            Inventory inv) {
        otherServer = s;
        pout = pOut;
        communicationTokens = tokens;
        inventory = inv;
    }
    
    /* run()
     *      Start TCPServerThread to process commands from a client communicating via TCP.
     *      Borrows from Dr. Garg's ServerThread.java class on EE360P Github.
     */
    public void run() {
        try {
            String response;
            // New requests include server/client designator token, so trim this off tokens
            //      to pass into reused inventory processing code.
            String[] tokens = Arrays.copyOfRange(communicationTokens, 1,
                communicationTokens.length - 1);
            // Send appropriate command to the server and display the
                // appropriate responses from the server
            mutexServerAccess();
            if (tokens[0].equals("purchase")) {
                response = inventory.processPurchase(tokens);
            } else if (tokens[0].equals("cancel")) {
                response = inventory.processCancel(tokens);
            } else if (tokens[0].equals("search")) {
                response = inventory.processSearch(tokens);
            } else if (tokens[0].equals("list")) {
                response = inventory.processList(tokens);
            } else {
                response = "ERROR: No such command";
            }
            pout.print(response);
            pout.flush();
            otherServer.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}

