
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class FairReadWriteLock {
	
        private PriorityBlockingQueue<Long> readQueue;
        private PriorityBlockingQueue<Long> writeQueue;
        private ThreadLocal<Long> timeStamp;

        public FairReadWriteLock() {
            this.readQueue = new PriorityBlockingQueue<>();
            this.writeQueue = new PriorityBlockingQueue<>();
            this.timeStamp = new ThreadLocal<Long>();
        }
        
    
	public synchronized void beginRead() throws InterruptedException {
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            timeStamp.set(ts.getTime());
            readQueue.add(Thread.currentThread().getId());
            while(this.timeStamp.get() >= writeQueue.peek()){
                this.wait();
            }
	}
	
	public synchronized void endRead() {
             readQueue.remove(timeStamp.get());
             this.notifyAll();
	}
	
	public synchronized void beginWrite() throws InterruptedException {
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            timeStamp.set(ts.getTime());
            writeQueue.add(Thread.currentThread().getId());
            while((this.timeStamp.get() >= writeQueue.peek()) || 
                    (this.timeStamp.get() >= readQueue.peek())){
                this.wait();
            }
	}
	public synchronized void endWrite() {
            writeQueue.remove(timeStamp.get());
            this.notifyAll();
	}
}
