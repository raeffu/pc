package pancake.parallel;

import mpi.MPI;

/**
 * Created by rlaubscher on 20.11.16.
 */
public class PancakeParallel {

  public enum Modes {
    solve, count
  }

  static final Modes MODE = Modes.solve;

  static int N = 5;
//  static int[] numbers = {31, 11, 40, 10, 38, 2, 13, 9, 37, 15, 25, 12, 5, 35, 23, 20, 7, 18, 30, 8, 39, 34, 36, 14, 6, 22, 27, 17, 33, 26, 3, 29, 1, 24, 28, 32, 21, 19, 16, 4};
//  static int[] numbers = {7, 18, 14, 1, 4, 26, 25, 22, 13, 2, 23, 3, 6, 19, 12, 5, 21, 27, 9, 10, 15, 30, 17, 11, 28, 24, 8, 20, 29, 16};
//static int[] numbers = {5, 4, 3, 2, 1, 6, 7, 8, 9, 10};
//static int[] numbers = {7, 10, 4, 1, 6, 3, 8, 5, 2, 9};
//static int[] numbers = {1, 2, 4, 6, 5, 3};
//static int[] numbers = {14, 10, 6, 15, 1, 16, 18, 20, 11, 3, 7, 13, 5, 17, 9, 12, 19, 4, 2, 8};
static int[] numbers = {2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15};

  public static void main(String[] args) {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int size = MPI.COMM_WORLD.Size();

    IWorker worker;

    if (MODE == Modes.solve) {
      if(rank == 0) {
        worker = new Master(numbers, size - 1);
      }
      else {
        worker = new Slave(rank);
      }
    }
    else {
      if(rank == 0) {
        worker = new pancake.parallel.count.Master(numbers, size - 1);
      }
      else {
        worker = new pancake.parallel.count.Slave(rank);
      }
    }

    worker.run();
    MPI.Finalize();
  }

}
