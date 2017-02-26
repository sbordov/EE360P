
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static HashMap<String, Integer> inventory = new HashMap<>();
    
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
    parseInventory();
    

    // TODO: handle request from clients
  }
  
  public static void parseInventory(){
      try(BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            stockItem(line);
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        String fileText = sb.toString();
    } catch (IOException ex) {
        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public static void stockItem(String input){
    String[] tokens = input.split("\\s+");
    if(tokens.length != 2){
        System.out.println("ERROR: Invalid inventory input.");
        return;
    }
    String item_name = tokens[0];
    int number_in_stock;
    try{
        number_in_stock = Integer.parseInt(tokens[1]);
    } catch(NumberFormatException e){
        number_in_stock = -1;
    }
    if(number_in_stock != -1){
        inventory.put(item_name, number_in_stock);
    }
  }
}
