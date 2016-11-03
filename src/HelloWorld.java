import mpi.MPI;

public class HelloWorld {

  public static void main(String[] args) {
    int rank;
    int size;
    int i;

    MPI.Init(args);

    rank = MPI.COMM_WORLD.Rank();

    if (rank == 0) {
      System.out.println("Hello World");
    } else {
      System.out.println("I am slave nr" + rank);
    }

    MPI.Finalize();
  }

}