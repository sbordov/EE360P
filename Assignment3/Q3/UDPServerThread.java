/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    public void run() {
        try {
            InetAddress ia = InetAddress.getByName(Symbols.nameServer);
            int port = Symbols.UDPPort;
            String command = new String(dataPacket.getData(), dataPacket.getOffset(),
                dataPacket.getLength(),StandardCharsets.UTF_8 // or some other charset
            );
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
            byte[] buffer = new byte[command.length()];
            buffer = response.getBytes();
            DatagramPacket returnPacket = new DatagramPacket(buffer, buffer.length, 
                    dataPacket.getAddress(), dataPacket.getPort());
            dataSocket.send(returnPacket);  
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
