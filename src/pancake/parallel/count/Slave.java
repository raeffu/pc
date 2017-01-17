package pancake.parallel.count;

import mpi.MPI;
import mpi.Request;
import pancake.parallel.IWorker;
import pancake.parallel.Master;
import pancake.parallel.Node;
import pancake.parallel.WorkSet;

import java.util.Stack;

/**
 * Created by Raphael Laubscher (laubr2)
 * Parallel Pancake sorting
 */
public class Slave implements IWorker {

  private int rank;
  private int[] no_data = new int[0];
  private Request stopSignal;

  public Slave(int rank) {
    this.rank = rank;
  }

  @Override public void run() {
    this.stopSignal = MPI.COMM_WORLD.Irecv(this.no_data, 0, 0, MPI.INT, Master.RANK, Master.KILL_TAG);

    while(stopSignal.Test() == null) {
      reportIdleWorker();

      // receive work
      WorkSet[] work = new WorkSet[1];
      MPI.COMM_WORLD.Recv(work, 0, 1, MPI.OBJECT, Master.RANK, Master.WORK_TAG);

      // I got work to do
      Stack<Node> stack = work[0].getStack();
      int bound = work[0].getBound();

      CountResult result = count(stack, bound);
      reportResult(result);
    }
  }

  private void reportResult(CountResult result) {
    CountResult[] buf = new CountResult[] {result};
    MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.OBJECT, Master.RANK, Master.RESULT_TAG);
  }

  private CountResult count(Stack<Node> stack, int bound) {
    int count = 0;
    int solutionBound = 0;
    boolean solutionFound = false;
    int nextBound = bound;

    while(stopSignal.Test() == null) {
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
              solutionFound = true;
              solutionBound = nextChild.getDepth();
              count++;
            }
            if (!solutionFound || solutionBound > depth) {
              stack.push(nextChild);
              stack.peek().nextNodes();
              depth++;
            }
          }
          else {
            nextBound = Math.min(nextBound, cost);
          }
        }
      }
      return new CountResult(count, nextBound);
    }
    return new CountResult(count, nextBound);
  }

  private void reportIdleWorker() {
    MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, Master.RANK, Master.IDLE_TAG);
  }
}
