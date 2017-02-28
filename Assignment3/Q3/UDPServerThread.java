/*
 * UDPServerThread
 * A class used for UDP communications to manage/sell inventory.
 */

/**
 *
 * @author Stefan
 */
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UDPServerThread extends Thread {
    DatagramPacket dataPacket;
    DatagramSocket dataSocket;
    Inventory inventory;
    
    public UDPServerThread(DatagramPacket packet, DatagramSocket s, Inventory inv) {
        dataPacket = packet;
        dataSocket = s;
        inventory = inv;
    }
    
    /* run()
     * Start UDPServerThread to process commands from a client communicating via TCP.
     */
    public void run() {
        try {
            // Read command from client.
            String command = new String(dataPacket.getData(), dataPacket.getOffset(),
                dataPacket.getLength(),StandardCharsets.UTF_8 // or some other charset
            );
            System.out.println("received:" + command);
            String[] tokens = command.split(" ");

            String response;
            // Send appropriate command to the server and display the
                // appropriate responses form the server
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
            byte[] buffer = new byte[command.length()];
            buffer = response.getBytes();
            // Create and send response to client
            DatagramPacket returnPacket = new DatagramPacket(buffer, buffer.length, 
                    dataPacket.getAddress(), dataPacket.getPort());
            dataSocket.send(returnPacket);  
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
