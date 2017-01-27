//UT-EID= sb39782


import java.util.concurrent.*;

public class PSort{
    private static ExecutorService threadPool;
    private static int[] A;

    private static class subArray implements Runnable{
        private int begin;
        private int end;

        public subArray(int beg_index, int end_index){
            begin = beg_index;
            end = end_index;
        }

        void quickSort(){
            Future f = null;
            int pivot = pivotArray(A, begin, end);

            if(pivot != -1){
                f = forkSubArray(A, begin, pivot);
                (new subArray(pivot + 1, end)).quickSort();
            }

            if(f != null) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        @Override
        public void run() {
            quickSort();
        }
    }

    public static void parallelSort(int[] A, int begin, int end){
        PSort.A = A;
        threadPool = Executors.newFixedThreadPool(10);
        (new subArray(begin, end)).quickSort();
        threadPool.shutdown();
    }

    public static Future forkSubArray(int[] A, int begin, int end){
        Future f = threadPool.submit(new subArray(begin, end));
        return f;
    }

    public synchronized static int pivotArray(int[] A, int begin, int end){

        if(begin == end){
            return -1;
        }

        int array_size = end - begin;
        if(array_size <= 1){ // Don't need to sort array of size 1 or 0.
            return -1;
        } else if(array_size <= 4){ // Sort array with insertion sort if size <= 4.
            iSort(A, begin, end);
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

    // Added insertion sorting for arrays of size 4 and smaller.
    public static void iSort(int[] A, int begin, int end){

        for(int i = begin; i < end; i++){
            int insertion = A[i];
            int j = i;
            while((j > begin) && (A[j - 1] > A[j])){
                int temp = A[j - 1];
                A[j - 1] = A[j];
                A[j] = temp;
                j--;
            }
            A[j] = insertion;
        }
    }
}
