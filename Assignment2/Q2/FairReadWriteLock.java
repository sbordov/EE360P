/*
 * sb39782
 * spf363
 */
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class FairReadWriteLock {
	
        private PriorityBlockingQueue<Long> readQueue;
        private PriorityBlockingQueue<Long> writeQueue;
        private ThreadLocal<Long> timeStamp;

        public FairReadWriteLock() {
            this.readQueue = new PriorityBlockingQueue<>(); // A queue of timestamps used to determine thread priority.
            this.writeQueue = new PriorityBlockingQueue<>(); // A queue of timestamps used to determine thread priority.
            this.timeStamp = new ThreadLocal<Long>(); // Timestamp local to each thread.
        }
        
    
	public synchronized void beginRead() throws InterruptedException {
            Timestamp ts = new Timestamp(System.currentTimeMillis()); // Get timestamp for current thread.
            timeStamp.set(ts.getTime()); // Set ThreadLocal.
            readQueue.add(timeStamp.get()); // Queue up this thread.
            while(!writeQueue.isEmpty() && (this.timeStamp.get() > writeQueue.peek())){
                this.wait(); // Wait if there are writers queued before this reader.
            }
	}
	
	public synchronized void endRead() {
             readQueue.remove(timeStamp.get()); // Remove reader from queue.
             this.notifyAll();
	}
	
	public synchronized void beginWrite() throws InterruptedException {
            Timestamp ts = new Timestamp(System.currentTimeMillis()); // Get writer timestamp.
            timeStamp.set(ts.getTime());
            writeQueue.add(timeStamp.get()); // Queue up writer.
            while((!writeQueue.isEmpty() && (this.timeStamp.get() > writeQueue.peek())) || 
                    (!readQueue.isEmpty() && (this.timeStamp.get() > readQueue.peek()))){
                this.wait(); // Wait if there is a writer or reader ahead of current writer in queue.
            }
	}
	public synchronized void endWrite() {
            writeQueue.remove(timeStamp.get()); // Remove current writer from queue.
            this.notifyAll();
	}
}
