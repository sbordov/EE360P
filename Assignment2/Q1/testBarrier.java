
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class testBarrier implements Runnable {
	final static int SIZE = 10;
	final static int ROUND = 10;
        final static int NUM_THREADS = 10;
	
	final MonitorCyclicBarrier barrier;
        
        protected ThreadLocal<String> local_output = new ThreadLocal<String>();
	
	public testBarrier(MonitorCyclicBarrier barrier) {
		this.barrier = barrier;
	}
	
	public void run() {
            int index = -1;
            StringBuffer output = new StringBuffer();
            for (int round = 0; round < ROUND; ++round) {
                    System.out.println("Thread " + Thread.currentThread().getId() + " is WAITING round:" + round);
                    output.append("Thread " + Thread.currentThread().getId() + " is WAITING round:" + round + "\n");
                    try {
                        index = barrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Thread " + Thread.currentThread().getId() + " is leaving round:" + round + " Index: " + index);
                    output.append("Thread " + Thread.currentThread().getId() + " is leaving round:" + round + " Index: " + index + "\n");
            }
            local_output.set(output.toString());
        }
        
        public String getLogs(){
            return (String) this.local_output.get();
        }
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
            MonitorCyclicBarrier barrier = new MonitorCyclicBarrier(SIZE);
            Thread[] t = new Thread[NUM_THREADS];
            
            PrintStream stdout = System.out;
            PrintStream out = new PrintStream(new FileOutputStream("hw2_q1_1.txt"));
            System.setOut(out);
            

            ArrayList<testBarrier> runnables = new ArrayList<>();
            testBarrier bar_thread;
            for (int i = 0; i < NUM_THREADS; ++i) {
                bar_thread = new testBarrier(barrier);
                runnables.add(bar_thread);
                t[i] = new Thread(bar_thread);
            }

            for (int i = 0; i < NUM_THREADS; ++i) {
                    t[i].start();
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(testBarrier.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            StringBuilder output = new StringBuilder();
            for(testBarrier thread : runnables){
                output.append(thread.getLogs());
            }
            
            System.setOut(stdout);
            CheckTestResults test = new CheckTestResults(ROUND, NUM_THREADS, SIZE, "hw2_q1_1.txt");
            test.check();
            
            
            /*
            try{  
               
                
                PrintWriter out = new PrintWriter( "hw2_q1_1.txt" );
                out.println( output.toString() );

            } catch (FileNotFoundException ex) {
                Logger.getLogger(testBarrier.class.getName()).log(Level.SEVERE, null, ex);
            }
*/
    }
}
