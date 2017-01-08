package pancake.parallel;

import mpi.MPI;
import pancake.Utility;

/**
 * Created by Raphael Laubscher (laubr2)
 *
 * MODE: set mode to SOLVE or COUNT
 * N: length of pankcake stack
 * numbers:  select between 3 options
 *           - enter array manually
 *           - generate random order -> Utility.randomOrder(N)
 *           - generate pair switched order -> Utility.switchedPairs(N)
 */
public class PancakeParallel {

  public enum Modes {
    SOLVE, COUNT
  }

  static final Modes MODE = Modes.SOLVE;

  static int N = 15;
//  static int[] numbers = {5, 4, 3, 2, 1, 6, 7, 8, 9, 10};
//  static int[] numbers = {7, 10, 4, 1, 6, 3, 8, 5, 2, 9};
//  static int[] numbers = {1, 2, 4, 6, 5, 3};
//  static int[] numbers = {14, 10, 6, 15, 1, 16, 18, 20, 11, 3, 7, 13, 5, 17, 9, 12, 19, 4, 2, 8};
//  static int[] numbers = {2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15};
//  static int[] numbers = Utility.randomOrder(N);
  static int[] numbers = Utility.switchedPairs(N);

  public static void main(String[] args) {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int size = MPI.COMM_WORLD.Size();

    IWorker worker;

    if(MODE == Modes.SOLVE) {
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
