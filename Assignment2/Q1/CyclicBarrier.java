/*
 * EID's of group members
 * 
 */
import java.util.concurrent.Semaphore; // for implementation using Semaphores

public class CyclicBarrier {
	Semaphore sema;
	Semaphore lock1 = new Semaphore(1, true);
	Semaphore lock2 = new Semaphore(1, true);
	Semaphore sema2;
	int parties;
	Integer index;
	
	public CyclicBarrier(int parties) {
		this.sema = new Semaphore(0, true);
		this.sema2 = new Semaphore(parties, true);
		this.parties = parties;
		this.index = parties;


	}
	
	public int await() throws InterruptedException {
		lock1.acquire();
		int i = getIndex();
		lock1.release();
		lock2.acquire();
		int len = sema.getQueueLength();

			if (len == parties - 1) {
				lock1.acquire();
				this.index = parties;
				lock1.release();
				sema.release(parties - 1);
				lock2.release();
				sema2.acquire(parties - 1);
				sema2.release(parties - 1);
			}
			else{
				lock2.release();
				sema2.acquire();
				sema.acquire();
				sema2.release();
			}
		return i;
	}
	public int getIndex(){
	return --index;
	}
}



