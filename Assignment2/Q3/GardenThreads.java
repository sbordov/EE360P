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
    Garden garden = new Garden();
    
    
    public static void main(String[] args){
        garden = new Garden();
        Thread Newton = 
    }
    
    private class Newton implements Runnable{

        @Override
        public void run() {
            while(true){
                garden.startDigging();
                dig();
                garden.doneDigging();
            }    
        }
    
    }
    
    private class Benjamin implements Runnable{

        @Override
        public void run() {
            while(true){
                garden.startSeeding();
                plantSeed();
                garden.doneSeeding();
            }   
        }
    
    }
    
    private class Mary implements Runnable{

        @Override
        public void run() {
            garden.startFilling();
            fill();
            garden.doneFilling();
        }
    
    }
}
    
}
