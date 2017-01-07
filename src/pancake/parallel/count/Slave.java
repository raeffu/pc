package pancake.parallel.count;

import mpi.MPI;
import mpi.Request;
import pancake.parallel.IWorker;
import pancake.parallel.Master;
import pancake.parallel.Node;
import pancake.parallel.WorkSet;

import java.util.Stack;

/**
 * Created by rlaubscher on 20.11.16.
 */
public class Slave implements IWorker {

  private int rank;
  private int[] no_data = new int[0];
  private Request stopSignal;

  private static final long WAIT_TIME = 5;

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

      System.out.println("Slave " + this.rank + "received work from Master");

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
    int nextcb = bound;

    while(stopSignal.Test() == null) {
      int cb = nextcb;
      nextcb = Integer.MAX_VALUE;
      int depth = stack.peek().getDepth();

      while(depth > 0 && depth <= bound){
        if(stack.peek().getSuccessors().empty()){
          stack.pop();
          depth--;
        }
        else {
          Node nextChild = stack.peek().getSuccessors().pop();
          int cost = nextChild.getDepth() + nextChild.getOptimisticDistanceToSolution();
          if (cost <= cb){
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
            nextcb = Math.min(nextcb, cost);
          }
        }
      }
      System.out.println("Slave " + this.rank + " found no solution with bound " + bound);
      System.out.println("Slave " + this.rank + " returns new bound " + nextcb);
      return new CountResult(count, nextcb);
    }
    System.out.println("Slave " + this.rank + " found no solution with bound " + bound);
    System.out.println("Slave " + this.rank + " returns new bound " + nextcb);
    return new CountResult(count, nextcb);
  }

  private void reportIdleWorker() {
    MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, Master.RANK, Master.IDLE_TAG);
  }
}
