import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FairReadWriteLock{
	boolean isWriting = false;
	int numReaders = 0;



	public synchronized void beginRead() {
		while(isWriting){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		numReaders++;
	}
	
	public synchronized void endRead() {
		numReaders--;
		if(numReaders == 0){
			this.notifyAll();
		}
	}
	
	public synchronized void beginWrite() {
		while(isWriting || numReaders > 0){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isWriting = true;
	}
	public synchronized void endWrite() {
		isWriting = false;
		this.notifyAll();
	}
}
