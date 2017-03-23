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
    
    protected int myId;
    protected int numFunctioningServers;
    protected LamportClock clock;
    protected PriorityQueue<ServerUpdateRequest> pendingQ;
    protected HashMap<Integer, ServerUpdateRequest> myProcesses;
    // A list of clients with key = processId. Specific to each server.
    protected HashMap<Integer, Socket> clients;
    protected HashMap<Integer, ServerInfo> serverList;
    protected Inventory inventory;
    
    public Server(int id, int numServers){
        this.myId = id;
        this.numFunctioningServers = numServers;
        this.clock = new LamportClock();
        this.pendingQ = new PriorityQueue<>(Symbols.INITIAL_QUEUE_CAPACITY, new ServerUpdateRequestComparator());
        this.clients = new HashMap<>();
        this.serverList = new HashMap<>();
        this.inventory = new Inventory();
        
    }
    
    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);
        int id, numServers;
        id = sc.nextInt();
        numServers = sc.nextInt();
        String inventoryPath = sc.next();
        Server myServer = new Server(id, numServers);

        System.out.println("[DEBUG] my id: " + id);
        System.out.println("[DEBUG] numServer: " + numServers);
        System.out.println("[DEBUG] inventory path: " + inventoryPath);
        for (int i = 0; i < myServer.getNumFunctioningServers(); i++) {
            // TODO: parse inputs to get the ips and ports of servers
            String str = sc.next();
            System.out.println("address for server " + i + ": " + str);
            myServer.getServerList().put(i + 1, new ServerInfo(str)); // Populate server list with info of servers.
        }
        // Parse the inventory file
        myServer.setInventoryFromFile(inventoryPath);
        myServer.listen();
  
    }
    
    public void listen(){
        while (true) {
            try {
                // Start server socket to communicate with clients and other servers
                System.out.println("Server " + this.myId + " started.");
                ServerSocket listener = new ServerSocket(this.getServerList().get(myId).getPortNumber());

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
  
    public void processRequest(Socket s){
        try {
            Scanner sc = new Scanner(s.getInputStream());
            String command = sc.nextLine();
            String[] tokens = command.split(";");
            Runnable requestProcessor = null;
            if(tokens[0].equals(Symbols.serverMessageHeader)){
                requestProcessor = new ServerRequestProcessor(s, tokens, this);
            } else if(tokens[0].equals(Symbols.clientMessageHeader)){
                requestProcessor = new ClientRequestProcessor(s, tokens, this);
            }
            if(requestProcessor != null){
                Thread t = new Thread(requestProcessor);
                t.start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public synchronized void insertToPendingQueue(ServerUpdateRequest request){
        this.pendingQ.offer(request);
    }
    
    
    public HashMap<Integer, ServerInfo> getServerList(){
        return this.serverList;
    }
    
    public int getNumFunctioningServers(){
        return this.numFunctioningServers;
    }
    
    public void setInventoryFromFile(String inventoryPath){
        inventory.parseInventory(inventoryPath);
    }
    
    public synchronized boolean incrementNumAcks(int processId)
            throws NullPointerException{
        ServerUpdateRequest process = myProcesses.get(processId);
        if(process == null){
            throw new NullPointerException("Process not found.");
        }
        int numAcks = process.incrementAndGetNumAcks();
        if(numAcks == numFunctioningServers - 1){
            return true;
        }
        return false;
    }
    
    public synchronized boolean checkNumAcks(int processId)
        throws NullPointerException{
        ServerUpdateRequest process = myProcesses.get(processId);
        if(process == null){
            throw new NullPointerException("Process not found.");
        }
        int numAcks = process.getNumAcks();
        if(numAcks == numFunctioningServers - 1){
            return true;
        }
        return false;
    }
    
    public synchronized boolean isProcessAtFrontOfQueue(int processId)
            throws NullPointerException {
        ServerUpdateRequest process = pendingQ.peek();
        if(process == null){
            throw new NullPointerException("Process not found.");
        }
        if((process.processId == processId) && (process.serverId == this.myId)){
            return true;
        } else{
            return false;
        }
    }
    
    // TODO: Make changes to inventory based on first ServerUpdateRequest in
    //      pendingQ, and remove ServerUpdateRequest from queue.
    public synchronized String performTransaction(){
        ServerUpdateRequest request = pendingQ.peek();
        String[] tokens = request.processTokens;
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
        pendingQ.poll(); // Remove latest entry in pendingQ post-inventory update.
        return response;
    }
}
