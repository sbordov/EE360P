
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
        /*
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
        */
        tcpPort = Symbols.ServerPort;
        udpPort = Symbols.UDPPort;
        String fileName = "C:\\Users\\Stefan\\Documents\\NetBeansProjects\\EE360P\\Assignment3\\Q3\\input\\inventory.txt";
        

        // Parse the inventory file
        Inventory inventory = new Inventory();
        inventory.parseInventory(fileName);


        // TODO: handle request from clients
        Thread tcpListener = new TCPRequestListener(tcpPort, inventory);
        Thread udpListener = new UDPRequestListener(udpPort, inventory);
        tcpListener.start();
        udpListener.start();

    }
    
    public static class TCPRequestListener extends Thread{
        private int port;
        private Inventory inventory;
        
        public TCPRequestListener(int portNum, Inventory inv){
            port = portNum;
            inventory = inv;
        }
        
        @Override
        public void run() {
            System.out.println("TCP Server started:");
            Server ns = new Server();
            try {
                ServerSocket listener = new ServerSocket(port);
                Socket s;
                while ( (s = listener.accept()) != null) {
                    Thread t = new TCPServerThread(ns.table, s, inventory);
                    t.start();
                }
            } catch (IOException e) {
                System.err.println("Server aborted:" + e);
            }
        }
    }
    
    public static class UDPRequestListener extends Thread{
        private int port;
        private Inventory inventory;
        
        public UDPRequestListener(int portNum, Inventory inv){
            port = portNum;
            inventory = inv;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("UDP Server started:");
                DatagramPacket datapacket;
                DatagramSocket datasocket = new DatagramSocket(port);
                byte[] buf = new byte[Symbols.packetSize];
                while (true) {
                    datapacket = new DatagramPacket(buf, buf.length);
                    datasocket.receive(datapacket);
                    Thread t = new UDPServerThread(datapacket, datasocket, inventory);
                    t.start();
                }
            } catch (SocketException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
  
    /*
    public static void takeTCPRequests(int tcpPort, Inventory inventory){
        System.out.println("TCP Server started:");
        Server ns = new Server();
        try {
            ServerSocket listener = new ServerSocket(tcpPort);
            Socket s;
            while ( (s = listener.accept()) != null) {
                Thread t = new TCPServerThread(ns.table, s, inventory);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }
    */
  
    /*
    public static void takeUDPRequests(int udpPort, Inventory inventory){
        try {
            System.out.println("UDP Server started:");
            DatagramPacket datapacket;
            DatagramSocket datasocket = new DatagramSocket(udpPort);
            byte[] buf = new byte[Symbols.packetSize];
            while (true) {
                datapacket = new DatagramPacket(buf, buf.length);
                datasocket.receive(datapacket);
                Thread t = new UDPServerThread(datapacket, datasocket, inventory);
                t.start();
            }
        } catch (SocketException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    */
    
}
