import mpi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeLeader {

  static int rank, size;
  // message types
  static final int IS_CANDIDATE = 0;
  static final int IS_LEADER = 1;

  // nb. of processes (i.e nodes of the graph)
  static final int N = 10;
  // incidence list of our graph
  //
  //   0 1
  //   |
  // 2-3-4  5-6-7
  //   |    |
  //   8    9
  //

  static final int incList[][] = {
      {3},           // 0
      {},            // 1
      {3},           // 2
      {0, 2, 4, 8},  // 3
      {3},           // 4
      {6, 9},        // 5
      {5, 7},        // 6
      {6},           // 7
      {3},           // 8
      {5}};          // 9

  static int[] neighbours;
  static int timeCap = 10;

  static int[] buf = new int[1];

  public static int findLeader() {
    int currentLeader = rank;
    buf[0] = currentLeader;

    // I am alone
    if(neighbours.length == 0) {
      return buf[0];
    }

    // leaf node
    if(neighbours.length == 1) {
      MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, neighbours[0], IS_CANDIDATE);
      MPI.COMM_WORLD.Recv(buf, 0, buf.length, MPI.INT, neighbours[0], IS_LEADER);
      return buf[0];
    }

    // I have friends

    List<Integer> received = new ArrayList<>(); // received from rank

    while (received.size() != neighbours.length - 1) {
      Status recv = MPI.COMM_WORLD.Recv(buf, 0, buf.length, MPI.INT, MPI.ANY_SOURCE, IS_CANDIDATE);
      int source = recv.source;

      if(buf[0] > currentLeader) {
        currentLeader = buf[0];
      }
      received.add(source);
    }

    int dest = Arrays.stream(neighbours)
        .filter(neighbour -> !received.contains(neighbour))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Not supposed to happen"));

    // send currentLeader
    buf[0] = currentLeader;

    // got answer from all but one neighbour, send candidate
    MPI.COMM_WORLD.Isend(buf, 0, buf.length, MPI.INT, dest, IS_CANDIDATE);

    Status recv = MPI.COMM_WORLD.Recv(buf, 0, buf.length, MPI.INT, dest, MPI.ANY_TAG);
    received.add(dest);

    // we did not get leader, but have all candidates so we know who is leader
    if(recv.tag == IS_CANDIDATE) {
      if(buf[0] > currentLeader) {
        currentLeader = buf[0];
      }
      buf[0] = currentLeader;
    }

    received.forEach(node -> MPI.COMM_WORLD.Isend(buf, 0, buf.length, MPI.INT, node, IS_LEADER));

    return currentLeader;
  }

  public static void main(String[] args) {
    MPI.Init(args);
    size = MPI.COMM_WORLD.Size();
    rank = MPI.COMM_WORLD.Rank();
    if(size != N)
      System.out.println("run with -n " + N);
    else
    {
      neighbours = incList[rank]; // our edges in the tree graph
      int leader = findLeader();
      System.out.println("******rank " + rank + ", leader: " + leader);
    }
    MPI.Finalize();
  }
}
 
