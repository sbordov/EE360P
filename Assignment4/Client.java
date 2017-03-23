import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    
    private int serverIndex;
    private int numServers;
    private Scanner din;
    private PrintStream pout;
    private Socket server;
    protected ArrayList<ServerInfo> serverList;
    
    public Client(int num){
        numServers = num;
        serverIndex = 0;
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
            ServerInfo myServer = serverList.get(0);
            String hostAddress = myServer.getIpAddress();
            int port = myServer.getPortNumber();
            server = new Socket(hostAddress, port);
            server.setSoTimeout(Symbols.TIMEOUT_DURATION);
            din = new Scanner(server.getInputStream());
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
            System.out.println("Processing command");
            if(server == null){
                throw new NullPointerException("Server connection is null.");
            }
            // Write command to server.
            pout.println(message.toString());
            pout.flush();
            System.out.println("Flushed command");
            StringBuilder sb = new StringBuilder("");
            String response;
            String prefix = "";
            // Read response from ServerSocket.
            System.out.println("Blocking read");
            while(din.hasNextLine()){
                response = din.nextLine();
                System.out.println(response);
                if(!response.equals(Symbols.assuranceMessage)){
                    sb.append(prefix).append(response);
                    if(prefix.equals("")){
                        prefix = "\n";
                    }
                }
            }
            System.out.println("Passed blocking read");
            System.out.println(sb.toString());
            //pout.close();
            //din.close();
            //server.close();
            System.out.println("Closing socket.");
            return sb.toString();
        } catch(java.net.SocketTimeoutException e){
            if(serverIndex < numServers){
                serverIndex++;
                processCommand(command, input);
            } else{
                System.out.println("Out of functioning servers.");
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return "ERROR: Nothing happened.";
    }
}
