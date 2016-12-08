package pancake;

import mpi.MPI;
import mpi.Request;

import java.util.Stack;

/**
 * Created by rlaubscher on 20.11.16.
 */
public class Slave implements IWorker {

  private int rank;
  private int[] no_data = new int[0];
  private Request solutionFound;

  private static final long WAIT_TIME = 5;

  public Slave(int rank) {
    this.rank = rank;
  }

  @Override public void run() {
    this.solutionFound = MPI.COMM_WORLD.Irecv(this.no_data, 0, 0, MPI.INT, Master.RANK, Master.KILL_TAG);

    while(solutionFound.Test() == null) {
      reportIdleWorker();



      // receive work
      WorkSet[] work = new WorkSet[1];
      MPI.COMM_WORLD.Recv(work, 0, 1, MPI.OBJECT, Master.RANK, Master.WORK_TAG);

      System.out.println("Slave " + this.rank);

      // I got work to do
      Node node = work[0].getNode();
//      System.out.println("received work " + node);
      int bound = work[0].getBound();

      SearchResult result = search(node, bound);
      reportResult(result);
    }
  }

  private void reportResult(SearchResult result) {
    SearchResult[] buf = new SearchResult[] {result};
    MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.OBJECT, Master.RANK, Master.RESULT_TAG);
  }

  private SearchResult search(Node node, int bound) {
    int f = node.getDepth() + node.getOptimisticDistanceToSolution();

    if (f > bound) {
      return new SearchResult(f);
    }

    if (node.isSolution()) {
      return new SearchResult(node);
    }

    int min = Integer.MAX_VALUE;
    Stack<Node> successors = node.nextNodes();

    // check if children contain solution or are out of bound
    for (Node succ : successors) {
      SearchResult r = search(succ, bound);
      if (r.solutionNode != null) {
        return r;
      }
      if (r.bound < min) {
        min = r.bound;
      }
    }
    return new SearchResult(min);
  }

  private void reportIdleWorker() {
    MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, Master.RANK, Master.IDLE_TAG);
  }
}
