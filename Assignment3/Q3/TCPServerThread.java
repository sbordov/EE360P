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
public class TCPServerThread extends Thread {
    NameTable table;
    Socket theClient;
    Inventory inventory;
    public TCPServerThread(NameTable table, Socket s, Inventory inv) {
        this.table = table;
        theClient = s;
        inventory = inv;
    }
    public void run() {
        try {
            Scanner sc = new Scanner(theClient.getInputStream());
            PrintWriter pout = new PrintWriter(theClient.getOutputStream());
            String command = sc.nextLine();
            System.out.println("received:" + command);
            String[] tokens = command.split(" ");

            String response;
            if (tokens[0].equals("purchase")) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
                response = inventory.processPurchase(tokens);
            } else if (tokens[0].equals("cancel")) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
                response = inventory.processCancel(tokens);
            } else if (tokens[0].equals("search")) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
                response = inventory.processSearch(tokens);
            } else if (tokens[0].equals("list")) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
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
