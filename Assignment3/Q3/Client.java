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
    private static String hostAddress;
    private static int tcpPort;
    private static int udpPort;
    
    public enum Protocol {
        TCP, UDP
    }
    
    public static void main (String[] args) {

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }

        // Set hostAddress and port numbers from input arguments.
        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        /*
         //Debugging code.
        tcpPort = Symbols.ServerPort;
        udpPort = Symbols.UDPPort;
        hostAddress = Symbols.nameServer;
        */
        

        Scanner sc = new Scanner(System.in);
        String response;
        // Read Client input and pass them along to Server using either UDP or TCP.
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");
            if (tokens[0].equals("setmode")) {
                setMode(tokens[1]);
            }
            else if (tokens[0].equals("purchase") || tokens[0].equals("cancel") ||
                    tokens[0].equals("search") || tokens[0].equals("list")) {
               response = processCommand(cmd, tokens); // Send command to server and process response.
               System.out.println(response);
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }
    
    /* getSocket()
     *    Creates a socket and input/output streams for TCP communication.
     */
    public static void getSocket() throws IOException {
        server = new Socket(hostAddress, tcpPort);
        din = new Scanner(server.getInputStream());
        pout = new PrintStream(server.getOutputStream());
    }
  
    /* setMode()
     *      Set the mode of communication (either tcp or udp).
     */
    public static void setMode(String input){
        if(input.equalsIgnoreCase("t")){
            protocol = Protocol.TCP;
        } else if(input.equalsIgnoreCase("u")){
            protocol = Protocol.UDP;
        }
    }
    
    /* processCommand()
     *      Process commands with either TCP or UDP protocol.
     */
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
    
    /* TCPCommand()
     *      Create a socket and write to a serverSocket via TCP
     */
    public static String TCPCommand(String command, String[] input){
        try {
            getSocket();
            // Write command to server.
            pout.println(command);
            pout.flush();
            StringBuilder sb = new StringBuilder("");
            String response;
            String prefix = "";
            // Read response from ServerSocket.
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
    
    /* UDPCommand()
     *      Communicate with Server via UDP by putting commands into packets.
     */
    public static String UDPCommand(String command, String[] input){
        String hostname = hostAddress;
        int port = udpPort;
        int len = Symbols.packetSize;
        byte[] rbuffer = new byte[len];
        DatagramPacket sPacket, rPacket;
        try {
            InetAddress ia = InetAddress.getByName(hostname);
            // Create a UDP socket.
            DatagramSocket datasocket = new DatagramSocket();
            byte[] buffer = new byte[command.length()];
            buffer = command.getBytes();
            // Load send packet with command and Server port information.
            sPacket = new DatagramPacket(buffer, buffer.length, ia, port);
            datasocket.send(sPacket);            	
            rPacket = new DatagramPacket(rbuffer, rbuffer.length);
            // Wait for return packet from server.
            datasocket.receive(rPacket);
            // Read response.
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
    
    /* invalidInputWarning()
     *      Warning message for invalid command type.
     */
    public static void invalidInputWarning(){
        System.out.println("ERROR: Invalid input.");
    }
    
}
