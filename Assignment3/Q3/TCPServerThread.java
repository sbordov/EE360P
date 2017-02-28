/*
 * TCPServerThread
 * A class used for TCP communications to manage/sell inventory.
 */

/**
 *
 * @author Stefan
 */
import java.net.*; import java.io.*; import java.util.*;
public class TCPServerThread extends Thread {
    Socket theClient;
    Inventory inventory;
    
    public TCPServerThread(Socket s, Inventory inv) {
        theClient = s;
        inventory = inv;
    }
    
    /* run()
     *      Start TCPServerThread to process commands from a client communicating via TCP.
     */
    public void run() {
        try {
            Scanner sc = new Scanner(theClient.getInputStream());
            PrintWriter pout = new PrintWriter(theClient.getOutputStream());
            String command = sc.nextLine();
            System.out.println("received:" + command);
            String[] tokens = command.split(" ");

            String response;
            // Send appropriate command to the server and display the
                // appropriate responses from the server
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
            theClient.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
