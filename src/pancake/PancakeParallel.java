package pancake;

import mpi.MPI;

/**
 * Created by rlaubscher on 20.11.16.
 */
public class PancakeParallel {
  static int N = 30;
  static int[] numbers = {7, 18, 14, 1, 4, 26, 25, 22, 13, 2, 23, 3, 6, 19, 12, 5, 21, 27, 9, 10, 15, 30, 17, 11, 28, 24, 8, 20, 29, 16};


  public static void main(String[] args) {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int size = MPI.COMM_WORLD.Size();

    IWorker worker;

    if (rank == 0) {
      worker = new Master(numbers, size - 1);
    } else {
      worker = new Slave(rank);
    }

    worker.run();
    MPI.Finalize();
  }

}
