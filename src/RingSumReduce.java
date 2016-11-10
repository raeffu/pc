import mpi.MPI;
import mpi.Request;

public class RingSumReduce {

  public static void main(String[] args) {
    int rank;
    int size;
    int source;
    int outBuffer[] = new int[1];
    int result[] = new int[1];


    MPI.Init(args);

    rank = MPI.COMM_WORLD.Rank();
    size = MPI.COMM_WORLD.Size();

    outBuffer[0] = rank;

    MPI.COMM_WORLD.Allreduce(outBuffer, 0, result, 0, outBuffer.length, MPI.INT, MPI.SUM);

    System.out.println(result[0]);

    MPI.Finalize();
  }

}