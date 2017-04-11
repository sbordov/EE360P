import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    
    private int serverIndex;
    private int numServers;
    //private Scanner din
    private BufferedReader din;
    private PrintStream pout;
    private Socket server;
    protected ArrayList<ServerInfo> serverList;
    
    public Client(int num){
        numServers = num;
        serverIndex = 1;
        serverList = new ArrayList<>();
    }
    
    public static void main (String[] args) {
        Scanner sc = new Scanner(System.in);
        int numServer = sc.nextInt();
        Client client = new Client(numServer);

        for (int i = 0; i < numServer; i++) {
          // TODO: parse inputs to get the ips and ports of servers
            String str = sc.next();
            System.out.println("address for server " + i + ": " + str);
            client.getServerList().add(new ServerInfo(str)); // Populate server list with info of servers.
        }
        if(sc.hasNext()){
            sc.nextLine();
        }
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");
            String response;

            if (tokens[0].equals("purchase") || tokens[0].equals("cancel") ||
                    tokens[0].equals("search") || tokens[0].equals("list")) {
                response = client.processCommand(cmd, tokens); // Send command to server and process response.
                if(!response.equals(""))
                    System.out.println(response);
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }
    
    public ArrayList<ServerInfo> getServerList(){
        return serverList;
    }
    
    /* getSocket()
     *    Creates a socket and input/output streams for TCP communication.
     */
    public void getSocket() throws IOException {
        if(!serverList.isEmpty()){
            ServerInfo myServer = serverList.get(serverIndex);
            String hostAddress = myServer.getIpAddress();
            int port = myServer.getPortNumber();
            server = new Socket();
            server.connect(new InetSocketAddress(hostAddress, port), 100);
            Reader reader = new InputStreamReader(server.getInputStream());
            din = new BufferedReader(reader);
            pout = new PrintStream(server.getOutputStream());
        }
    }
    
     /* TCPCommand()
     *      Create a socket and write to a serverSocket via TCP
     */
    public String processCommand(String command, String[] input){
        try {
            StringBuilder message = new StringBuilder();
            message.append(Symbols.clientMessageHeader);
            message.append(Symbols.messageDelimiter);
            message.append(command);
            getSocket();
            if(server == null){
            }
            // Write command to server.
            pout.println(message.toString());
            pout.flush();
            StringBuilder sb = new StringBuilder("");
            String response;
            String prefix = "";
            // Read response from ServerSocket.
            do{
                response = din.readLine();
                if(response != null){
                    sb.append(prefix).append(response);
                    if(prefix.equals("")){
                        prefix = "\n";
                    }
                }
            } while(response != null);
            //pout.close();
            //din.close();
            //server.close();
            return sb.toString();
        } catch (IOException ex) {
            if(serverIndex < numServers - 1){
                serverIndex++;
                try {
                    getSocket();
                } catch (IOException ex1) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex1);
                }
                processCommand(command, input);
            } else{
            }
        } 
        return "";
    }
}
