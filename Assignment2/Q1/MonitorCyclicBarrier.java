/*
 * EID's of group members
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class MonitorCyclicBarrier {
    int num_parties;
    int barrier_index;
    int released_threads;
    
    public MonitorCyclicBarrier(int parties) {
        this.num_parties = parties;
        this.barrier_index = parties - 1;
    }

    public int await() throws InterruptedException {
       int index = getIndex();
       while(index > 0){
           try{
               wait();
           } catch(InterruptedException e){
               
           }
       }
       notifyAll();
       // Threads begin entering. getIndex();
       // Threads hit wait until barrier_index = parties.
       // thread[parties - 1] notifies parties - 1 threads to proceed.
       // After all threads are gone, start letting num_parties other threads wait at barrier.
       
      // you need to write this code
        return index;
    }
    
    public synchronized int getIndex(){
        while(barrier_index < 0){
            try{
                wait();
            } catch(InterruptedException e){
                
            }
        }
        notify();
        return barrier_index--;
    }
    
    public synchronized void resetIndex(){
        barrier_index = num_parties - 1;
        notifyAll();
    }
}
