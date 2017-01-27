//UT-EID= sb39782


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
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
            int pivot = pivotArray(A, begin, end);
            if(pivot != -1){
                parallelSort(A, begin, pivot);
                parallelSort(A, pivot + 1, end);
            }
        }
  }
  
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function 
    PSort.A = A;
    threadPool.execute(new sortingThread(begin, end));
    
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
