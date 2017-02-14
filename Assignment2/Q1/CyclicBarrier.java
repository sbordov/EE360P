/*
 * sb39782
 * spf363
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class CyclicBarrier {
    int parties; // Number of parties to wait for each cycle.
    Integer barrier_index; // Thread arrival index.
    
    Semaphore index_lock = new Semaphore(1); // Locks access to thread arrival index.
    Semaphore zero_index_lock; // Locks access to index generation when index = 0.
    Semaphore queue_length_access_lock = new Semaphore(1); // Locks access to checking barrier_permit queue length.
    
    Semaphore barrier_permit; // Used to block the first <parties> arriving threads until all have arrived.
    Semaphore barrier_exit_permit; // Semaphore for telling <# parties>th thread when all predecessor threads have finished.
    
    Semaphore last_thread_permit;

    public CyclicBarrier(int parties) {
        this.barrier_permit = new Semaphore(0);
        this.barrier_exit_permit = new Semaphore(parties - 1);
        this.parties = parties;
        this.barrier_index = parties;
        this.last_thread_permit = new Semaphore(0);
        zero_index_lock = new Semaphore(parties);
    }

    public int await() throws InterruptedException {
        index_lock.acquire(); // Lock index generation.
        int index = getIndex(); // Generate arrival index.
        index_lock.release(); // Unlock index generation.
        //queue_length_access_lock.acquire();
        //int len = barrier_permit.getQueueLength();
        if(index == 0){
        //if (len == parties - 1) { // For the <# parties>th thread to arrive to barrier.
            //index_lock.acquire(); // Lock index so it may be reset.
            this.barrier_index = parties; // Reset barrier_index for new threads.
            //index_lock.release(); // Unlock index.
            barrier_permit.release(parties - 1); // Release <parties - 1> permits to the threads which arrived previous to this one.
            last_thread_permit.acquire(parties - 1); // Wait for <parties - 1> predecessor threads to acquire barrier_permit.
            //queue_length_access_lock.release(); // Allow other threads to queue up for the cyclic barrier.
            barrier_exit_permit.release(parties - 1); // Release permits for more threads to try to acquire barrier_permit.
            zero_index_lock.release(parties); // Allow threads to retrieve index.
        }
        else{
            //queue_length_access_lock.release(); // Release queue_length access.
            barrier_exit_permit.acquire(); // If blocked here, need to wait for predecessor threads to flush.
            barrier_permit.acquire(); // Try to acquire barrier permit. Wait until <party>th thread arrives.
            last_thread_permit.release(); // Release permits to signal completion to <party>th thread.
        }
        return index; // Return arrival index.
    }
    
    
    /* Return a thread's arrival index. The first thread to arrive will get index <parties - 1>, 
     *  with the last thread getting index 0.
     */
    public int getIndex() throws InterruptedException{
        zero_index_lock.acquire(); // getIndex() needs to be locked if index hasn't been reset from zero.
        return --barrier_index; // Return pre-incremented barrier_index.
    }
}



