import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    private static int numServers;
    private static int myId;
    private static double myTs = Double.POSITIVE_INFINITY;
    private static PriorityQueue<Integer> pendingQ = new PriorityQueue<>();
    private static int numOkay = 0;
    private static HashMap<Integer, ServerInfo> servers = new HashMap<>();
    private static Inventory inventory = new Inventory();
    
    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);
        myId = sc.nextInt();
        numServers = sc.nextInt();
        String inventoryPath = sc.next();

        System.out.println("[DEBUG] my id: " + myId);
        System.out.println("[DEBUG] numServer: " + numServers);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);
        for (int i = 0; i < numServers; i++) {
            // TODO: parse inputs to get the ips and ports of servers
            String str = sc.next();
            System.out.println("address for server " + i + ": " + str);
            servers.put(i, new ServerInfo(str)); // Populate server list with info of other servers.
        }
  
        while (true) {
            try {
                // Start server socket to communicate with clients and other servers
                System.out.println("TCP Server started:");
                ServerSocket listener = new ServerSocket(servers.get(myId).getPortNumber());

                // Parse the inventory file
                inventory.parseInventory(inventoryPath);

                // TODO: handle request from client
                Socket s;
                while ( (s = listener.accept()) != null) {
                    processRequest(s);
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    }
  
    public static void processRequest(Socket s){
        try {
            Scanner sc = new Scanner(s.getInputStream());
            PrintWriter pout = new PrintWriter(s.getOutputStream());
            String command = sc.nextLine();
            String[] tokens = command.split(" ");
            if(tokens[0].equals("SERVER CONNECTION")){
                Thread t = new ServerRequestProcessor(s, pout, tokens, inventory);
                t.start();
            } else if(tokens[0].equals("CLIENT CONNECTION")){
                Thread t = new ClientRequestProcessor(s, pout, tokens, inventory);
                t.start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
  
}
