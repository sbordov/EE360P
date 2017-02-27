
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
    private static HashMap<Integer, Order> orders = new  HashMap<>();
    private static int order_count = 1;
    
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
        String user_name = tokens[1];
        String product_name = tokens[2];
        int quantity = Integer.parseInt(tokens[3]);
        if(!checkForProduct(product_name)){
            reply("Not Available - We do not sell this product.");
            System.out.println("Not Available - We do not sell this product.");
            return;
        } else if(!productAvailable(product_name, quantity)){
            reply("Not Available - Not enough items");
            System.out.println("Not Available - Not enough items");
            return;
        }
        makeNewOrder(user_name, product_name, quantity);
    }
    
    public synchronized void processCancel(String[] tokens){
        int order_id = Integer.parseInt(tokens[1]);
        if(!checkForOrder(order_id)){
            reply(order_id + " not found, no such order.");
            return;
        }
        removeOrder(order_id);
    }
    
    public synchronized void processSearch(String[] tokens){
        String user_name = tokens[1];
        boolean order_found = false;
        for(int order_id : orders.keySet()){
            Order o = orders.get(order_id);
            if(o.user_name.equals(user_name)){
                order_found = true;
                reply(o.id + ", " + o.product_name, ", " + o.quantity);
            }
        }
        if(!order_found){
            reply("No order found for " + user_name);
        }
    }
    
    public synchronized void processList(String[] tokens){
        for(String product : inventory.keySet()){
            reply(product + " " +  inventory.get(product));
        }
        
    }
    
    public void makeNewOrder(String user_name, String product_name, int quantity){
        Order this_order = new Order(getOrderNumber(), user_name, product_name, quantity);
        orders.put(this_order.id, this_order);
        int new_item_quantity = inventory.get(product_name) - quantity;
        inventory.put(product_name, new_item_quantity);
        reply("Your order has been placed, " + this_order.id + " " + user_name + " " + 
                product_name + " " + quantity);
        
    }
    
    public synchronized int getOrderNumber(){
        return order_count++;
    }
    
    public boolean checkForProduct(String product){
        return inventory.containsKey(product);
    }
    
    public boolean productAvailable(String product, int quantity){
        return inventory.get(product) >= quantity;
    }
    
    public boolean checkForOrder(int order_id){
        return orders.containsKey(order_id);
    }
    
    public void removeOrder(int order_id){
        Order this_order = orders.get(order_id);
        int quantity = this_order.quantity;
        String product = this_order.product_name;
        int new_item_quantity = inventory.get(product) + quantity;
        inventory.put(product, new_item_quantity);
        orders.remove(order_id);
        reply("Order " + order_id + " is cancelled.");
    }
}
