package exercises;

import mpi.MPI;

public class PingPong {

  public static void main(String[] args) {
    int rank;
    int size;
    int i;
    int outBuffer[] = new int[200000];
    int inBuffer[] = new int[200000];
    final int PING = 100;
    final int PONG = 101;
    long start0 = 0;
    long start1 = 0;
    long end0 = 0;
    long end1 = 0;

    MPI.Init(args);

    rank = MPI.COMM_WORLD.Rank();

    start0 = System.nanoTime();
    for (int j = 0; j < 1000; j++)
    {

      if(rank == 0)
      {
//        System.out.println("send p0 --> p1");
        MPI.COMM_WORLD.Send(outBuffer, 0, outBuffer.length, MPI.INT, 1, 0);
//        System.out.println("recv p0 <-- p1");
        MPI.COMM_WORLD.Recv(inBuffer, 0, inBuffer.length, MPI.INT, 1, 0);
      }
      else
      {
//        System.out.println("recv p1 <-- p0");
        MPI.COMM_WORLD.Recv(inBuffer, 0, inBuffer.length, MPI.INT, 0, 0);
//        System.out.println("send p1 --> p0");
        MPI.COMM_WORLD.Send(outBuffer, 0, outBuffer.length, MPI.INT, 0, 0);
      }
    }
    end0 = System.nanoTime();
    long time = (end0 - start0)/1000000;
    System.out.println(time);


    MPI.Finalize();
  }

}