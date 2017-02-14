
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
            readQueue.add(timeStamp.get());
            while(!writeQueue.isEmpty() && (this.timeStamp.get() > writeQueue.peek())){
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
            writeQueue.add(timeStamp.get());
            while((!writeQueue.isEmpty() && (this.timeStamp.get() > writeQueue.peek())) || 
                    (!readQueue.isEmpty() && (this.timeStamp.get() > readQueue.peek()))){
                this.wait();
            }
	}
	public synchronized void endWrite() {
            writeQueue.remove(timeStamp.get());
            this.notifyAll();
	}
}
