package pancake.parallel.count;

import mpi.MPI;
import pancake.parallel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Raphael Laubscher (laubr2)
 * Parallel Pancake sorting
 */
public class Master implements IWorker {
  public static final int RANK = 0;
  public static final int IDLE_TAG = 10;
  public static final int WORK_TAG = 20;
  public static final int KILL_TAG = 66;
  public static final int RESULT_TAG = 30;
  private int nSlaves;
  private int[] slaves;
  private int[] no_data = new int[0];
  private Node root;
  private LinkedBlockingQueue<Integer> idleWorkers;
  private List<IdleListener> iListeners = new ArrayList<>();
  private List<ResultListener> rListeners = new ArrayList<>();
  private LinkedBlockingQueue<Result> results = new LinkedBlockingQueue<>();
  private int nAnswers = 0;

  public Master(int[] state, int nSlaves) {
    this.nSlaves = nSlaves;
    this.slaves = new int[nSlaves];
    this.root = new Node(state, 0, null);
    this.root.addPlate();
  }

  @Override public void run() {

    // save listener messages from idle workers
    this.idleWorkers = new LinkedBlockingQueue<>();
    for (int i = 0; i < nSlaves; i++) {
      IdleListener idleListener = new IdleListener(i + 1, this.idleWorkers);
      idleListener.start();
      iListeners.add(idleListener);
      ResultListener resultListener = new ResultListener(i + 1, this.results);
      resultListener.start();
      rListeners.add(resultListener);
      this.slaves[i] = i + 1;
    }

    // COUNT

    long start = System.currentTimeMillis();
    int count = 0;

    try {
      count = count(this.root);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    long end = System.currentTimeMillis();

    // stop workers
    for (int slave : this.slaves) {
      MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, slave, Master.KILL_TAG);
    }

    this.iListeners.forEach(IdleListener::stopListening);
    this.rListeners.forEach(ResultListener::stopListening);

    System.out.println("\n\n\n\n");
    System.out.println("########## FINISHED ##########\n");
    System.out.format("Time: %dms\n", end - start);
    System.out.format("Initial state: %s\n", root);
    System.out.format("Number of optimal solutions: %d\n", count);
    System.exit(0);
  }

  private int count(Node root) throws InterruptedException {
    if(root.isSolution()) return 1;
    boolean solutionFound = false;
    int bound = root.getOptimisticDistanceToSolution();

    while (!solutionFound) {
      int count = 0;
      this.results.clear();
      nAnswers = 0;
      int nextBound = Integer.MAX_VALUE;
      root.nextNodes();

      while (!root.getSuccessors().empty()) {
        Stack<Node> stack = new Stack<>();
        stack.push(root);
        stack.push(root.getSuccessors().pop());
        stack.peek().nextNodes();
        if (stack.peek().isSolution()) {
          return 1;
        }

        Integer worker = idleWorkers.take();
        WorkSet work = new WorkSet(stack, bound);
        WorkSet[] workBuffer = new WorkSet[] {work};

        System.out.println("Send work to slave " + worker + ": " + workBuffer[0].getStack().peek());
        MPI.COMM_WORLD.Isend(workBuffer, 0, 1, MPI.OBJECT, worker, Master.WORK_TAG);
        nAnswers++;
      }

      // receive result
      System.out.println("receive result");

      while (this.results.size() < nAnswers) {
        Result result = this.results.take();
        nAnswers--;
        CountResult r = result.getCountResult();
        int slave = result.getSlave();
        System.out.println("Count Result, received by Master from " + slave + ": " + r);
        if(r.count != 0) {
          System.out.println("\nSolution found, increase counter!\n");
          count += r.count;
        }
        nextBound = Math.min(nextBound, r.bound);
      }

      // all solutions counted for this bound
      if (count > 0) {
        return count;
      }

      // no solution found
      System.out.println(">>>>>>>>>>>>> new bound: " + nextBound);
      bound = nextBound;
    }
    return 0;
  }

}
