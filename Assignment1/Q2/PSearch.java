//UT-EID= sb39782
//        spf363


import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PSearch{
    /*
        This method creates as many threads as specied by numThreads, divides the array A into that many
        parts, and gives each thread a part of the array to search for x sequentially. If any thread nds x,
        then it returns an index i such that A[i] = x. Otherwise, the method returns -1. Use Callable
        interface for your implementation. Note that, you can assume that every element of the array is
        unique.
    */
    
    public static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static ArrayList<SearchableArrayChunk> chunks; // List of ArrayChunk-containing threads.
    public static ArrayList<Future<Integer>> futures; // List of futures for chunks.
    public static int searchForThisNumber; // Number each thread searches for.
    
  public static int parallelSearch(int k, int[] A, int numThreads){
    // TODO: Implement your parallel search function
    searchForThisNumber = k;
    chunks = SearchableArrayChunk.createSearchChunks(A, numThreads); // Break array into searchable chunks.
    futures = new ArrayList<>();
    for(SearchableArrayChunk chunk : chunks){
        Future<Integer> f = threadPool.submit(chunk); // Submit array chunk threads for running.
        futures.add(f);
    }
    for(Future<Integer> f : futures){ // Wait for each future value.
        try {
            if(f.get() != -1){
                return f.get(); // Return index of array value if found.
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(PSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    return -1; // if not found
  }
  
  /*
    ArrayChunk is a class which wraps an array and specifies a consecutive chunk of the wrapped array.
  */
  private static class ArrayChunk{
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
      
      // Break up an input array into multiple ArrayChunks.
      public static ArrayList<ArrayChunk> chunkify(int[] A, int numChunks){
          int length = A.length;
          if((length == 0) || (numChunks == 0)){ // For empty input array or zero chunks
              return null;
          }
          if(numChunks > length){ // If want more chunks than elements in A, split A into as many 1 element pieces as possible.
              return singleElementChunks(A, numChunks);
          } else{
              return multipleElementChunks(A, numChunks);
          }
      }
      
      // Create a list of ArrayChunks where each chunk is of size zero.
      public static ArrayList<ArrayChunk> singleElementChunks(int[] A, int numChunks){
          ArrayList<ArrayChunk> chunks = new ArrayList<>();
          for(int i = 0; i < A.length; i++){
                  ArrayChunk chunk = new ArrayChunk(A, i, i + 1);
                  chunks.add(chunk);
          }
          return chunks;
      }
      
      // Create a list of ArrayChunks from an Array where each chunk is of size ((A.length / numChunks) + 1) or size A.length / numChunks.
      public static ArrayList<ArrayChunk> multipleElementChunks(int[] A, int numChunks){
          ArrayList<ArrayChunk> chunks = new ArrayList<>();
          int length = A.length;
          int chunkSize = length / numChunks;
          // When length % numChunks != zero, there will be <extraElements> ArrayChunks of size (length / numChunks) + 1.
          int extraElements = length % numChunks;
          ArrayChunk chunk;
          int j = 0;
          for(int i = 0; i < numChunks; i++){
              if(extraElements > 0){ // Create chunks of size (length / numChunks) + 1.
                  chunk = new ArrayChunk(A, j, j + chunkSize + 1);
                  j += (chunkSize + 1);
                  extraElements--;
              } else{ // Create chunk of size length / numChunks.
                  chunk = new ArrayChunk(A, j, j + chunkSize);
                  j += chunkSize;
              }
              chunks.add(chunk);
          }
          return chunks;
      }
      
      public int sequentialSearch(){
          for(int i = this.begin_index; i < this.end_index; i++){
              if(this.array[i] == searchForThisNumber){
                  return i;
              }
          }
          return -1;
      }
  }
  
  /*
    SearchableArrayChunk is an adaptor class which adapts an ArrayChunk to the Callable interface,
        allowing it to be used for a sequential search in parallel with other ArrayChunks.
  */
  private static class SearchableArrayChunk implements Callable<Integer>{
        public ArrayChunk chunk;
        
        public SearchableArrayChunk(ArrayChunk ch){
            chunk = ch;
        }
        
        public static ArrayList<SearchableArrayChunk> createSearchChunks(int[] A, int numChunks){
            ArrayList<ArrayChunk> chunks = ArrayChunk.chunkify(A, numChunks);
            ArrayList<SearchableArrayChunk> searchChunks = new ArrayList<>();
            for(ArrayChunk chunk: chunks){
                searchChunks.add(new SearchableArrayChunk(chunk));
            }
            return searchChunks;
        }
        
        @Override
        public Integer call() throws Exception {
            return chunk.sequentialSearch();
        }
    
  }
}
