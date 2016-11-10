import mpi.MPI;
import mpi.Request;

public class RingSum {

  public static void main(String[] args) {
    int rank;
    int size;
    int source;
    int sum[] = new int[1];

    sum[0] = 0;

    MPI.Init(args);

    rank = MPI.COMM_WORLD.Rank();
    size = MPI.COMM_WORLD.Size();
    source = rank - 1;
    if(source < 0)
      source = size - 1;

    Request recvHandle = MPI.COMM_WORLD.Irecv(sum, 0, sum.length, MPI.INT, source, MPI.ANY_TAG);
    if(rank != 0)
    {
      recvHandle.Wait();
      sum[0] += rank;
    }

    MPI.COMM_WORLD.Isend(sum, 0, sum.length, MPI.INT, ((rank + 1) % size), MPI.ANY_TAG);

    if(rank == size - 1)
      System.out.println(sum[0]);

    MPI.Finalize();
  }

}