
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
public class GardenThreads {    
    public static Garden garden;
    public static Newton newton;
    public static Benjamin ben;
    public static Mary mary;
    
    
    public static void main(String[] args){
        garden = new Garden();
        newton = new Newton();
        ben = new Benjamin();
        mary = new Mary();
        Thread t1 = new Thread(newton);
        Thread t2 = new Thread(ben);
        Thread t3 = new Thread(mary);
        t1.start();
        t2.start();
        t3.start();
        try {
            Thread.sleep(1000);
             t1.join();
             t2.join();
             t3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GardenThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static class Newton implements Runnable{
        
        @Override
        public void run() {
            while(true){
                garden.startDigging();
                dig();
                garden.doneDigging();
            }    
        }
        
        public void dig(){
            int holesDug = garden.totalHolesDugByNewton();
            System.out.println("Digging hole number " + Integer.toString(holesDug));
        }
    
    }
    
    private static class Benjamin implements Runnable{

        @Override
        public void run() {
            while(true){
                garden.startSeeding();
                plantSeed();
                garden.doneSeeding();
            }
        }
        
        public void plantSeed(){
            int seedsPlanted = garden.totalHolesSeededByBenjamin();
            System.out.println("Planting seed number " + Integer.toString(seedsPlanted));
        }
    
    }
    
    private static class Mary implements Runnable{

        @Override
        public void run() {
            while(true){
                garden.startFilling();
                fill();
                garden.doneFilling();
            }
        }
    
        public void fill(){
            int holesFilled = garden.totalHolesFilledByMary();
            System.out.println("Filling hole number " + Integer.toString(holesFilled));
        }
    }
    
}
