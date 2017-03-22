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
        String[] input = (String[]) inputTokens.get();
    }

    @Override
    protected void send(String message) {
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
     *      Start TCPServerThread to process commands from a client communicating via TCP.
     *      Borrows from Dr. Garg's ServerThread.java class on EE360P Github.
     */
    public void run() {
        try {
            String response;
            processInput();
            // New requests include server/client designator token, so trim this off tokens
            //      to pass into reused inventory processing code.
            /*
            String[] request = (String[]) requestTokens.get();
            String[] tokens = Arrays.copyOfRange(request, 1,
                request.length - 1);
            */
            // Send appropriate command to the server and display the
                // appropriate responses from the server
            mutexServerAccess();
            if (inputTokens.get()[0].equals("purchase")) {
                response = myServer.inventory.processPurchase(inputTokens.get());
            } else if (inputTokens.get()[0].equals("cancel")) {
                response = myServer.inventory.processCancel(inputTokens.get());
            } else if (inputTokens.get()[0].equals("search")) {
                response = myServer.inventory.processSearch(inputTokens.get());
            } else if (inputTokens.get()[0].equals("list")) {
                response = myServer.inventory.processList(inputTokens.get());
            } else {
                response = "ERROR: No such command";
            }
            PrintWriter pOut = (PrintWriter) pout.get();
            pOut.print(response);
            pOut.flush();
            Socket s = (Socket) otherServer.get();
            s.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }


}

