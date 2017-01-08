package pancake.parallel;

import mpi.MPI;
import mpi.Request;

import java.util.Stack;

/**
 * Created by Raphael Laubscher (laubr2)
 * Parallel Pancake sorting
 */
public class Slave implements IWorker {

  private int rank;
  private int[] no_data = new int[0];
  private Request solutionFound;

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

      System.out.println("Slave " + this.rank + "received work from Master");

      // I got work to do
      Stack<Node> stack = work[0].getStack();
      int bound = work[0].getBound();

      SearchResult result = search(stack, bound);
      reportResult(result);
    }
  }

  private void reportResult(SearchResult result) {
    SearchResult[] buf = new SearchResult[] {result};
    MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.OBJECT, Master.RANK, Master.RESULT_TAG);
  }

  private SearchResult search(Stack<Node> stack, int bound) {
    int nextBound = bound;

    while(solutionFound.Test() == null) {
      int currentBound = nextBound;
      nextBound = Integer.MAX_VALUE;
      int depth = stack.peek().getDepth();

      while(depth > 0 && depth <= bound){
        if(stack.peek().getSuccessors().empty()){
          stack.pop();
          depth--;
        }
        else {
          Node nextChild = stack.peek().getSuccessors().pop();
          int cost = nextChild.getDepth() + nextChild.getOptimisticDistanceToSolution();
          if (cost <= currentBound){
            if (nextChild.isSolution()){
              return new SearchResult(nextChild);
            }
            stack.push(nextChild);
            stack.peek().nextNodes();
            depth++;
          }
          else {
            nextBound = Math.min(nextBound, cost);
          }
        }
      }
      System.out.println("Slave " + this.rank + " found no solution with bound " + bound);
      System.out.println("Slave " + this.rank + " returns new bound " + nextBound);
      return new SearchResult(nextBound);
    }
    return new SearchResult(nextBound);
  }

  private void reportIdleWorker() {
    MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, Master.RANK, Master.IDLE_TAG);
  }
}
