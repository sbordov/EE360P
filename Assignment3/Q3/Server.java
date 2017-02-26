
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static NameTable table;
    
    public Server() {
        table = new NameTable();
    }
    
    
    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(2) <udpPort>: the port number for UDP connection");
            System.out.println("\t(3) <file>: the file of inventory");

            System.exit(-1);
        }
        tcpPort = Integer.parseInt(args[0]);
        udpPort = Integer.parseInt(args[1]);
        String fileName = args[2];

        // Parse the inventory file
        Inventory inventory = new Inventory();
        inventory.parseInventory(fileName);


        // TODO: handle request from clients
        takeTCPRequests(tcpPort, inventory);

    }
  
    public static void takeTCPRequests(int tcpPort, Inventory inventory){
        Server ns = new Server();
        System.out.println("Server started:");
        try {
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            while ( (s = listener.accept()) != null) {
                Thread t = new ServerThread(ns.table, s, inventory);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }
  
    public static void takeUDPRequests(int udpPort, Inventory inventory){

    }
  
}
