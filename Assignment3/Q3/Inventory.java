
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class Inventory {
    private static HashMap<String, Integer> inventory = new HashMap<>();
    
    public Inventory(){
        
    }
    
    public void parseInventory(String fileName){
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while (line != null) {
                stockItem(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
    public void stockItem(String input){
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
    
    public synchronized void processPurchase(String[] tokens){
        
    }
    
    public synchronized void processCancel(String[] tokens){
        
    }
    
    public synchronized void processSearch(String[] tokens){
        
    }
    
    public synchronized void processList(String[] tokens){
        
    }
}
