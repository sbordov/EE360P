all: multiply


test: multiply
	mpiexec ./multiply -n 3

clean:
	rm result.txt multiply

multiply:
	mpicc matrixMPI.c -o multiply


