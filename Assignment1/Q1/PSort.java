//UT-EID= sb39782


import java.util.*;
import java.util.concurrent.*;

public class PSort{
  public static void parallelSort(int[] A, int begin, int end){
    // TODO: Implement your parallel sort function 
    int pivot = pivotArray(A, begin, end);
    if(pivot != -1){
        parallelSort(A, begin, pivot - 1);
        parallelSort(A, pivot + 1, end);
    }
  }
  
  public static int pivotArray(int[] A, int begin, int end){
    int i = 0;
    int pivot = A[end - 1]; // Pick pivot to be last element of (sub)array.
    while(i < (end - 1)){
        int j = 0;
        if(A[i] > pivot){
            while(j != (end - 1)){
                if(A[j] > pivot){
                    j++;
                }
            }
            swap(A, i, j);
            if(j == (end - 1)){
                return i; // Return new index of old pivot.
            }
        }
        i++;
    }
    return -1;
  }
  
  public static void swap(int[] A, int first, int second){
      int temp = A[first];
      A[first] = A[second];
      A[second] = temp;
  }
}
