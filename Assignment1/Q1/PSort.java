//UT-EID= sb39782


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  private static ExecutorService threadPool;
  private static int[] A;
    
  private static class sortingThread implements Runnable{
        private int begin;
        private int end;
        
        public sortingThread(int beg_index, int end_index){
            begin = beg_index;
            end = end_index;
        }

        @Override
        public void run() {
            Future f = null;
            int pivot = pivotArray(A, begin, end);

            if(pivot != -1){
                f = parallelSort2(A, begin, pivot);
                (new sortingThread(pivot + 1, end)).run();
            }
            if(f != null) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return;
            }
            return;
        }

  }
  
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function 
    PSort.A = A;
     threadPool = Executors.newFixedThreadPool(10);
      try {
          parallelSort2(A, begin, end).get();
      } catch (InterruptedException e) {
          e.printStackTrace();
      } catch (ExecutionException e) {
          e.printStackTrace();
      }
      threadPool.shutdown();
      try {
          threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
      }

  }
  public static Future parallelSort2(int[] A, int begin, int end){
      Future f = threadPool.submit(new sortingThread(begin, end));
      return f;
  }
  
  public synchronized static int pivotArray(int[] A, int begin, int end){
    if(begin == end){
        return -1;
    }
    int pivot = A[end - 1]; // Pick pivot to be last element of (sub)array.
    int i = begin;
    int j = begin;
    while(j < (end - 1)){
        while((i < (end - 1)) && (A[i] <= pivot)){
            i++;
        }
        j = i + 1;
        while((j < (end - 1)) && (A[j] >= pivot)){
            j++;
        }
        if(j < end){
            swap(A, i, j);
        }
    }
    return i;
  }
  
  public static void swap(int[] A, int first, int second){
      int temp = A[first];
      A[first] = A[second];
      A[second] = temp;
  }
}
