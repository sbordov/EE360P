/*
 * sb39782
 * spf363
 */

public class Order {
    public int id;
    public String user_name;
    public String product_name;
    public int quantity;
    
    public Order(int order_id, String orderer, String product, int qty){
        id = order_id;
        user_name = orderer;
        product_name = product;
        quantity = qty;
    }
    
}
