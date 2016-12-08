package pancake;

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

    System.out.format("\nSorted after %d flips\n", solution.getDepth());
    System.out.println(Arrays.toString(solution.getState()));

    System.out.format(">>>>>>> Time: %dms \n", end - start);

    this.iListeners.forEach(IdleListener::stopListening);
    this.rListeners.forEach(ResultListener::stopListening);

    // stop workers
    for (int slave : this.slaves) {
      MPI.COMM_WORLD.Isend(this.no_data, 0, 0, MPI.INT, slave, Master.KILL_TAG);
    }

  }

  private Node solve(Node root) throws InterruptedException {
    Node solutionNode = null;
    int bound = root.getOptimisticDistanceToSolution();
    int maxBound = 2 * root.getState().length;

    Stack<Node> children = root.nextNodes();
    workStack.push(new State(root, children, bound));

    System.out.println("roots children");
    for (Node child : children) {
      System.out.println(child);
    }

    while (bound < maxBound) {
      nAnswers = 0;
      this.results.clear();
      State current = workStack.pop();
      Node node = current.getNode();
      Stack<Node> successors = current.getChildren();
      int currentBound = current.getBound();

      System.out.println("currentBound: " + currentBound);

      for (Node succ : successors) {
        // send work
        while (idleWorkers.size() < 1) {
          try {
            System.out.println("No idle workers");
            Thread.sleep(5);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        Integer worker = idleWorkers.take();
        WorkSet work = new WorkSet(succ, currentBound);
        WorkSet[] workBuffer = new WorkSet[] {work};
        System.out.println("Send work to slave " + worker + ": " + workBuffer[0].getNode().toString());
        MPI.COMM_WORLD.Isend(workBuffer, 0, 1, MPI.OBJECT, worker, Master.WORK_TAG);
        nAnswers++;
      }

      // receive result
      System.out.println("receive result");

      while (this.results.size() < nAnswers) {
        while (this.results.isEmpty()) {
          try {
            System.out.println("sleep");
            Thread.sleep(WAIT_TIME);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        for (Result result : results) {
          SearchResult r = result.getSearchResult();
          int slave = result.getSlave();
          System.out.println("Search Result, received by Master from " + slave + ": " + r.toString());

          if(r.solutionNode != null) {
            System.out.println("\nSolution found, stop all the shit!\n");
            return r.solutionNode;
          }
        }

      }
      // no solution found
      System.out.println(">>>>>>>>>>>>> increment bound");
      bound++;
      workStack.push(new State(root, children, bound));

    }

    // go back in stack if no result found

    return solutionNode;
  }

}
