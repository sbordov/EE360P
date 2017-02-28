import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static Protocol protocol;
    private static Scanner din;
    private static PrintStream pout;
    private static Socket server;
    public static void getSocket() throws IOException {
        server = new Socket(Symbols.nameServer, Symbols.ServerPort);
        din = new Scanner(server.getInputStream());
        pout = new PrintStream(server.getOutputStream());
    }
    
    public enum Protocol {
        TCP, UDP
    }
    
    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;

        /*
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);
        */
        tcpPort = Symbols.ServerPort;
        udpPort = Symbols.UDPPort;
        hostAddress = Symbols.nameServer;

        Scanner sc = new Scanner(System.in);
        String response;
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

            if (tokens[0].equals("setmode")) {
                // TODO: set the mode of communication for sending commands to the server 
                // and display the name of the protocol that will be used in future
                setMode(tokens[1]);
            }
            else if (tokens[0].equals("purchase") || tokens[0].equals("cancel") ||
                    tokens[0].equals("search") || tokens[0].equals("list")) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
               response = processCommand(cmd, tokens);
               System.out.println(response);
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }
  
    public static void setMode(String input){
        if(input.equalsIgnoreCase("t")){
            protocol = Protocol.TCP;
        } else if(input.equalsIgnoreCase("u")){
            protocol = Protocol.UDP;
        }
    }
    
    public static String processCommand(String command, String[] input){
        switch(protocol){
            case TCP:
                return TCPCommand(command, input);
            case UDP:
                return UDPCommand(command, input);
            default:
                
                break;
        }
        return "Nothing doing";
        
        
    }
    
    public static String TCPCommand(String command, String[] input){
        try {
            getSocket();
            pout.println(command);
            pout.flush();
            StringBuilder sb = new StringBuilder("");
            String response;
            String prefix = "";
            while(din.hasNextLine()){
                response = din.nextLine();
                sb.append(prefix).append(response);
                if(prefix.equals("")){
                    prefix = "\n";
                }
            }
            server.close();
            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERROR: Nothing happened.";
    }
    
    public static String UDPCommand(String command, String[] input){
        String hostname = Symbols.nameServer;
        int port = Symbols.UDPPort;
        int len = Symbols.packetSize;
        byte[] rbuffer = new byte[len];
        DatagramPacket sPacket, rPacket;
        try {
            InetAddress ia = InetAddress.getByName(hostname);
            DatagramSocket datasocket = new DatagramSocket();
            byte[] buffer = new byte[command.length()];
            buffer = command.getBytes();
            sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
            datasocket.send(sPacket);            	
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            datasocket.receive(rPacket);
            String retstring = new String(rPacket.getData(), 0,
                rPacket.getLength());
            return retstring;
        } catch (UnknownHostException e) {
            System.err.println(e);
        } catch (SocketException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return "ERROR: Failure to retrieve data.";
    }
    
    public static void invalidInputWarning(){
        System.out.println("ERROR: Invalid input.");
    }
    
}
