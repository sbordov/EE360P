#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

enum {
    REQUEST,
    PRODUCT,
    INDEX,
    ROW
};
//

void *schedule(void *vectorSize) {
    FILE *fp;
    int world_size;
    int vs = *((int *) vectorSize);
    int *buf = malloc(sizeof(int) * vs);
    int *result = malloc(sizeof(int) * vs);
    int rowNum;
    int product;
    int stored = 0;
    int index;
    MPI_Status status;
    /*DEBUG*/ printf("scheduler opening matrix\n");
    fp = fopen("matrix.txt", "r");
    fscanf(fp, "%d", &rowNum);
    for (int j = 0; j < rowNum; j++) {
        for (int i = 0; i < vs; i++) {
            fscanf(fp, "%d", &buf[i]);
        }

        /*DEBUG*/ printf("scheduler waiting on request\n");
        MPI_Recv(&index, 1, MPI_INT, MPI_ANY_SOURCE, REQUEST, MPI_COMM_WORLD,
                 &status);
        /*DEBUG*/ printf("scheduler receive request %d from %d\n", index, status.MPI_SOURCE);
        if (index >= 0) {
            /*DEBUG*/ printf("scheduler waiting on product from %d on index %d\n", status.MPI_SOURCE, index);
            MPI_Recv(&product, 1, MPI_INT, status.MPI_SOURCE, PRODUCT, MPI_COMM_WORLD,
                     &status);
            /*DEBUG*/ printf("scheduler receive product %d from %d on index %d\n", product, status.MPI_SOURCE, index);
            result[index] = product;
            stored++;
        } else {
            world_size = -index;
        }
        /*DEBUG*/ printf("scheduler sending index %d to %d\n", j, status.MPI_SOURCE);
        MPI_Send(&j, 1, MPI_INT, status.MPI_SOURCE, INDEX, MPI_COMM_WORLD);
        /*DEBUG*/ printf("scheduler sending row %d to %d\n", j, status.MPI_SOURCE);
        MPI_Send(buf, vs, MPI_INT, status.MPI_SOURCE, ROW, MPI_COMM_WORLD);
        //wait for request
        //if was not an initial request, store result
        //send new index as confirmation that there is another row
        // send row
    }

    while (stored < rowNum) {
                /*DEBUG*/ printf("scheduler waiting on request\n");
        MPI_Recv(&index, 1, MPI_INT, MPI_ANY_SOURCE, REQUEST, MPI_COMM_WORLD,
                 &status);
        /*DEBUG*/ printf("scheduler receive request %d from %d\n", index, status.MPI_SOURCE);
        if (index >= 0) {
            /*DEBUG*/ printf("scheduler waiting on product from %d on index %d\n", status.MPI_SOURCE, index);
            MPI_Recv(&product, 1, MPI_INT, status.MPI_SOURCE, PRODUCT, MPI_COMM_WORLD,
                     &status);
            /*DEBUG*/ printf("scheduler receive product %d from %d on index %d\n", product, status.MPI_SOURCE, index);
            result[index] = product;
            stored++;
          }
    }

    /*DEBUG*/ printf("scheduler closing matrix\n");
    fclose(fp);
    int halt = -1;
    printf("world size is %d\n", world_size);
    for (int i = 1; i < world_size; i++) {
        /*DEBUG*/ printf("scheduler sending halt to %d\n", i);
        MPI_Send(&halt, 1, MPI_INT, i, INDEX, MPI_COMM_WORLD);
    }
    /*DEBUG*/ printf("scheduler opening result.txt\n");
    fp = fopen("result.txt", "w");
    int k;
    for (k = 0; k < (rowNum - 1); k++) {
        /*DEBUG*/ printf("scheduler writing %d to result.txt spot %d\n", result[k], k);
        fprintf(fp, "%d ", result[k]);
    }
    if (vs > 0) {
        /*DEBUG*/ printf("scheduler writing %d to result.txt spot %d\n", result[rowNum - 1], rowNum - 1);
        fprintf(fp, "%d", result[rowNum - 1]);
    }
    /*DEBUG*/ printf("scheduler sending halt to 0\n");
    MPI_Send(&halt, 1, MPI_INT, 0, INDEX, MPI_COMM_WORLD);
    /*DEBUG*/ printf("scheduler exiting\n");
    fclose(fp);
    pthread_exit(0);
}

int main(int argc, char **argv) {

    // Initialize the MPI environment. The two arguments to MPI Init are not
    // currently used by MPI implementations, but are there in case future
    // implementations might need the arguments.
    int provided;
    MPI_Init_thread(&argc, &argv, MPI_THREAD_MULTIPLE, &provided);
    // Get the number of processes
    int world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);
    // Get the rank of the process
    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
    // Get the name of the processor
    char processor_name[MPI_MAX_PROCESSOR_NAME];
    int name_len;
    MPI_Get_processor_name(processor_name, &name_len);
    // Print off a hello world message
    printf("Hello world from processor %s, rank %d out of %d processors\n",
           processor_name, world_rank, world_size);


    int vectorSize;
    int vector[1025];
    int rows;
    if (world_rank == 0) {
        FILE *fp = fopen("vector.txt", "r");
        while (fscanf(fp, "%d", &vector[vectorSize]) != -1) {
            vectorSize++;
        }
        fclose(fp);
        fp = fopen("matrix.txt", "r");
        fscanf(fp, "%d", &rows);
        fclose(fp);
    }
    MPI_Bcast(&vectorSize, 1, MPI_INT, 0, MPI_COMM_WORLD);
    MPI_Bcast(&vector, vectorSize, MPI_INT, 0, MPI_COMM_WORLD);
    MPI_Bcast(&rows, 1, MPI_INT, 0, MPI_COMM_WORLD);
    pthread_t scheduler;
    if (world_rank == 0) {
        pthread_create(&scheduler, NULL, schedule, &vectorSize);
    }
    int index = -world_size;
/*DEBUG*/ printf("%d sending init\n", world_rank);
    MPI_Send(&index, 1, MPI_INT, 0, REQUEST, MPI_COMM_WORLD);
/*DEBUG*/ printf("%d waiting on initial index\n", world_rank);
    MPI_Recv(&index, 1, MPI_INT, 0, INDEX, MPI_COMM_WORLD,
             MPI_STATUS_IGNORE);
/*DEBUG*/ printf("%d recieved initial index %d\n", world_rank, index);
    if (index == -1) {
        if (world_rank == 0) {
            void *exit;
            pthread_join(scheduler, exit);
        }
        /*DEBUG*/ printf("%d terminiating soon\n", world_rank);
        MPI_Finalize();
        return 0;
    }
    while (index != -1) {
        int buf[vectorSize];
        int res = 0;
        /*DEBUG*/ printf("%d waiting on row %d\n", world_rank, index);
        MPI_Recv(&buf, vectorSize, MPI_INT, 0, ROW, MPI_COMM_WORLD,
                 MPI_STATUS_IGNORE);
        /*DEBUG*/ printf("%d recieved row %d\n", world_rank, index);
        for (int p = 0; p < vectorSize; p++) {
            res += vector[p] * buf[p];
        }
        /*DEBUG*/ printf("%d sending index %d\n", world_rank, index);
        MPI_Send(&index, 1, MPI_INT, 0, REQUEST, MPI_COMM_WORLD);
        /*DEBUG*/ printf("%d sending product %d for index %d\n", world_rank, res, index);
        MPI_Send(&res, 1, MPI_INT, 0, PRODUCT, MPI_COMM_WORLD);
        /*DEBUG*/ printf("%d waiting on index\n", world_rank);
        MPI_Recv(&index, 1, MPI_INT, 0, INDEX, MPI_COMM_WORLD,
                 MPI_STATUS_IGNORE);
        /*DEBUG*/ printf("%d recieved index %d\n", world_rank, index);
    }
/*DEBUG*/ printf("%d terminiating soon\n", world_rank);
    // Finalize the MPI environment. No more MPI calls can be made after this
    if (world_rank == 0) {
        /*DEBUG*/ printf("trying to join\n");
        void *exit;
        pthread_join(scheduler, exit);
        printf("joined successfully\n");
    }
    MPI_Finalize();
}