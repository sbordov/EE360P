//UT-EID= sb39782


import java.util.*;
import java.util.concurrent.*;


public class PSearch{
    /*
        This method creates as many threads as specied by numThreads, divides the array A into that many
        parts, and gives each thread a part of the array to search for x sequentially. If any thread nds x,
        then it returns an index i such that A[i] = x. Otherwise, the method returns -1. Use Callable
        interface for your implementation. Note that, you can assume that every element of the array is
        unique.
    */
    
  public static int parallelSearch(int k, int[] A, int numThreads){
    // TODO: Implement your parallel search function 
    return -1; // if not found
  }
  
  public static int sequentialSearch(ArrayChunk array, int k){
      for(int i = begin; i < end; i++){
          if(A[i] == k){
              return i;
          }
      }
      return -1;
  }
  
  public class ArrayChunk{
      private int[] array;
      private int begin_index; // Begin index is inclusive.
      private int end_index; // End index is exclusive.
      
      public ArrayChunk(){
          begin_index = -1;
          end_index = -1;
      }
      
      public ArrayChunk(int[] A, int begin, int end){
          array = A;
          begin_index = begin;
          end_index = end;
      }
      
      public ArrayList<ArrayChunk> chunkify(int[] A, int numChunks){
          ArrayList<ArrayChunk> chunks = new ArrayList<>();
          int length = A.length;
          if((length == 0) || (numChunks == 0)){ // For empty input array or zero chunks
              return null;
          }
          if(numChunks > length){ // If want more chunks than elements in A, split A into as many 1 element pieces as possible.
              for(int i = 0; i < length; i++){
                  ArrayChunk chunk = new ArrayChunk(A, i, i + 1);
                  chunks.add(chunk);
              }
          } else{ // TODO: Split array into even chunks.
              int chunkSize = length / numChunks;
              int extraElements = length % numChunks;
              for(int i = 0; i < numChunks; i++){
                  if(extraElements > 0)
                      ArrayChunk chunk = new ArrayChunk(A, i, i + chunkSize);
                      extraElements--;
                  }
              }
          }
      }
  }
}
