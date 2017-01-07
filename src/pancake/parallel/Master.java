package pancake.parallel;

import mpi.MPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rlaubscher on 20.11.16.
 * Parallel Pancake sorting
 */
public class Master implements IWorker {
  public static final int RANK = 0;
  public static final int IDLE_TAG = 10;
  public static final int WORK_TAG = 20;
  public static final int KILL_TAG = 66;
  public static final int RESULT_TAG = 30;
  private int[] state;
  private int nSlaves;
  private int[] slaves;
  private int[] no_data = new int[0];
  private Node root;
  private Stack<State> workStack = new Stack<>();
  private Stack<Node> nodes = new Stack<>();
  private LinkedBlockingQueue<Integer> idleWorkers;
  private List<IdleListener> iListeners = new ArrayList<>();
  private List<ResultListener> rListeners = new ArrayList<>();
  private LinkedBlockingQueue<Result> results = new LinkedBlockingQueue<>();
  private int nAnswers = 0;
  private static final long WAIT_TIME = 5;

  public Master(int[] state, int nSlaves) {
    this.state = state;
    this.nSlaves = nSlaves;
    this.slaves = new int[nSlaves];
    this.root = new Node(state, 0, null);
//    int[] newState = Arrays.copyOf(root.getState(), root.getState().length + 1);
//    newState[newState.length - 1] = root.getState().length + 1;
//    root.setState(newState);
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

    // solve
    System.out.println("initial configuration:");
    System.out.println(Arrays.toString(this.state) + "\n");

    long start = System.currentTimeMillis();
    Node solution = null;

    try {
      solution = solve(this.root);
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

    System.out.format("\nSorted after %d flips\n", solution.getDepth());
    System.out.format(">>>>>>> Time: %dms \n", end - start);
    System.out.println(solution);
    Node parent = solution.getParent();
    while(parent != null) {
      System.out.println(parent);
      parent = parent.getParent();
    }

  }

  private Node solve(Node root) throws InterruptedException {
    if(root.isSolution()) return root;
    boolean solutionFound = false;
    int bound = root.getOptimisticDistanceToSolution();

    while (!solutionFound) {
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
          return stack.peek();
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
        SearchResult r = result.getSearchResult();
        int slave = result.getSlave();
        System.out.println("Search Result, received by Master from " + slave + ": " + r);
        if(r.solutionNode != null) {
          System.out.println("\nSolution found, stop all workers!\n");
          return r.solutionNode;
        }
        nextBound = Math.min(nextBound, r.bound);
      }

      // no solution found
      System.out.println(">>>>>>>>>>>>> new bound: " + nextBound);
      bound = nextBound;
    }
    return null;
  }

}