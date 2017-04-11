all: multiply


test1: multiply
	mpiexec ./multiply -n 1

test3: multiply
	mpiexec ./multiply -n 3

test15: multiply
	mpiexec ./multiply -n 15

clean:
	rm result.txt multiply

multiply:
	mpicc matrixMPI.c -o multiply


