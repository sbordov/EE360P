/*
 * sb39782
 * spf363
 */

public class MonitorCyclicBarrier {
    int num_parties;
    int barrier_index;
    int released_threads;
    boolean index_resetting;
    
    public MonitorCyclicBarrier(int parties) {
        this.num_parties = parties;
        this.barrier_index = parties - 1;
        index_resetting = false;
    }

    public int await() throws InterruptedException {
       int index = getIndex(); // Threads not in current round will wait in getIndex.
       
       synchronized(this){
           while(!index_resetting){ // Once index_resetting is high, the current (#parties) threads have all arrived.
               try{
                   wait(); // Wait until partieth thread arrives.
               } catch(InterruptedException e){

               }
           }
           if(index == 0){ // The (#parties)eth thread will notify all waiting threads.
               this.notifyAll();
           }
       }
       resetIndex(); // Exiting threads reset index values.
       return index;
    }
    
    // Allows threads to retrieve an index of arrival. The first thread will receive
    //  an index of parties - 1, the last index will receive an index of 0.
    public synchronized int getIndex(){
        while(index_resetting){
            try{
                wait();
            } catch(InterruptedException e){
                
            }
        }
        if(barrier_index == 0){
            index_resetting = true;
        }
        //notifyAll();
        return barrier_index--;
    }
    
    // Called by exiting threads to restore index value.
    public synchronized void resetIndex(){
        barrier_index++;
        if(barrier_index == num_parties - 1){ // After all threads have exited, begin filling barrier again.
            index_resetting = false;
            notifyAll();
        }
    }
}
